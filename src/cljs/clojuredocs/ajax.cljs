(ns clojuredocs.ajax
  (:require [goog.net.XhrIo]
            [clojure.string :as str]))

(defn validate-ajax-args [{:keys [method]}]
  (let [valid-http-methods #{:get
                             :post
                             :put
                             :patch
                             :delete
                             :options
                             :head
                             :trace
                             :connect}]
    (when-not (get valid-http-methods method)
      (throw (str "nsfw.dom/ajax: "
                  method
                  " is not a valid ajax method ("
                  (->> valid-http-methods
                       (map pr-str)
                       (interpose ", ")
                       (apply str))
                  ")")))))

(defn safe-name [o]
  (when o
    (name o)))

(defn safe-upper-case [s]
  (when s
    (str/upper-case s)))

(def ajax-defaults
  {:path "/"
   :method "GET"
   :data {}
   :success (fn []
              (throw "nsfw.dom/ajax: Unhandled :success callback from AJAX call."))
   :error (fn []
            (throw "nsfw.dom/ajax: Unhandled :error callback from AJAX call."))})

(defn parse-headers [s]
  (when s
    (->> (str/split s #"\r\n")
         (mapcat (fn [header]
                   (->> (str/split header ":")
                        (map str/trim))))
         (apply hash-map))))

(defn req->resp [req]
  {:headers (parse-headers (.getAllResponseHeaders req))
   :status (.getStatus req)
   :body (.getResponseText req)
   :success (.isSuccess req)})

(defn format-body [{:keys [headers body] :as r}]
  (let [content-type (or (-> headers
                             (get "content-type"))
                         (-> headers
                             (get "Content-Type"))
                         "")
        body (condp #(re-find % content-type) content-type
               #"application/json" (-> body
                                       JSON/parse
                                       (js->clj :keywordize-keys true))
               #"application/edn" (reader/read-string body)
               body)]
    (assoc r :body body)))

(defn ajax [opts]
  (let [opts (merge ajax-defaults opts)
        opts (if-not (:headers opts)
               (assoc opts
                 :headers (condp = (:data-type opts)
                            :json {"Content-Type" "application/json"}
                            :edn {"Content-Type" "application/edn"}
                            {"Content-Type" "application/edn"}))
               opts)
        opts (cond
               (= :json (:data-type opts))
               (assoc opts :data (-> (:data opts)
                                     clj->js
                                     JSON/stringify))

               (= :edn (:data-type opts))
               (assoc opts :data (pr-str (:data opts)))

               :else opts)
        {:keys [path method data headers success error data-type]} opts]
    (validate-ajax-args opts)
    (goog.net.XhrIo/send
      path
      (fn [e]
        (try
          (let [req (.-target e)
                resp (-> req
                         req->resp
                         format-body)]
            (if (:success resp)
              (success resp)
              (error resp)))
          (catch js/Object e
            (.error js/console (.-stack e))
            (throw e))))
      (-> method
          name
          safe-upper-case)
      data
      (clj->js headers))))
