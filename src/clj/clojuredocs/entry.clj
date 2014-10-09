(ns clojuredocs.entry
  (:use [ring.middleware
         file
         file-info
         session
         params
         nested-params
         multipart-params
         keyword-params]
        [ring.middleware.session.cookie :only (cookie-store)]
        [ring.util.response :only (response content-type)])
  (:require [clojuredocs.config :as config]
            [compojure.core :refer (defroutes GET POST PUT DELETE context)]
            [compojure.response :refer (Renderable render)]
            [compojure.route :refer (not-found)]
            [ring.util.response :refer (redirect)]
            [clojure.string :as str]
            [hiccup.page :refer (html5)]
            [clojuredocs.env :as env]
            [clojuredocs.util :as util]
            [clojuredocs.pages.common :as common]
            [clojuredocs.pages :as pages]
            [clojure.pprint :refer (pprint)]
            [clojuredocs.api.server :as api.server]
            [somnium.congomongo :as mon]
            [clojure.edn :as edn]
            [clojuredocs.search :as search]
            [prone.middleware :as prone]))

(defn decode-body [content-length body]
  (when (and content-length
             (> content-length 0))
    (let [buf (byte-array content-length)]
      (.read body buf 0 content-length)
      (.close body)
      (String. buf))))

(defn response-body
  "Turn a InputStream into a string."
  [{:keys [content-length body]}]
  (if (string? body)
    body
    (decode-body content-length body)))

(defn hiccup->html-string [body]
  (if-not (vector? body)
    body
    (let [bodys (if (= :html5 (first body))
                  (rest body)
                  [body])]
      (html5 bodys))))

;; Extend hiccup to support rendering of hiccup vectors
(extend-protocol Renderable
  clojure.lang.PersistentVector
  (render [v request]
    (render (hiccup->html-string v) request))

  clojure.lang.APersistentMap
  (render [resp-map _]
    (if (-> resp-map :body vector?)
      (-> resp-map
          (update-in [:headers "Content-Type"] #(or % "text/html;charset=utf-8"))
          (assoc :body (-> resp-map :body hiccup->html-string)))
      (merge (with-meta (response "") (meta resp-map))
             resp-map))))

(defn redirect-to-var [ns name]
  (fn [r]
    (let [name (->> name
                    util/cd-decode
                    util/cd-encode)]
      {:status 301
       :headers {"Location" (str "/" ns "/" name)}})))

(defn old-v-page-redirect [id]
  (fn [r]
    (try
      (let [{:keys [ns name] :as legacy-var}
            (mon/fetch-one :legacy-var-redirects :where {:function-id (Integer/parseInt id)})]
        (when legacy-var
          {:status 307
           :headers {"Location" (str "/" ns "/" (util/cd-encode name))}}))
      (catch Exception e nil))))

(defroutes old-url-redirects
  (GET "/clojure_core/:ns/:name" [ns name] (redirect-to-var ns name))
  (GET "/clojure_core/:version/:ns/:name" [ns name] (redirect-to-var ns name))
  (GET "/quickref/*" [] {:status 301 :headers {"Location" "/quickref"}})
  (GET "/clojure_core" [] {:status 301 :headers {"Location" "/core-library"}})
  (GET "/clojure_core/:ns" [ns] {:status 301 :headers {"Location" (str "/" ns)}})
  (GET "/v/:id" [id] (old-v-page-redirect id))
  (GET "/examples_style_guide" [] {:status 301 :headers {"Location" "/examples-styleguide"}})
  (GET "/Clojure%20Core/:ns" [ns] {:status 301 :headers {"Location" (str "/" ns)}})
  (GET "/Clojure%20Core" [] {:status 301 :headers {"Location" "/core-library"}})
  (GET "/clojure_core" [] {:status 301 :headers {"Location" "/core-library"}}))

(defroutes _routes
  (context "/api" [] api.server/routes)
  (var pages/routes)
  ;; Redirect old urls
  (var old-url-redirects)
  (not-found (fn [r] (common/four-oh-four r))))

(def session-store
  (cookie-store
    {:key (env/str :session-key)
     :domain ".clojuredocs.org"}))

(defn promote-session-user [h]
  (fn [{:keys [session] :as r}]
    (h (assoc r :user (:user session)))))

(defn wrap-long-caching [h]
  (fn [r]
    (let [res (h r)
          res-ct (get-in res [:headers "Content-Type"])
          content-types #{"text/css"
                          "text/javascript"
                          "application/font-woff"
                          "font/opentype"
                          "application/vnd.ms-fontobject"
                          "image/svg+xml"
                          "application/x-font-ttf"}]
      (if (get content-types res-ct)
        (update-in res [:headers] merge {"Cache-Control" "public, max-age=31536000"})
        res))))

(defn edn-content-type? [{:keys [headers]}]
  (when-let [ct (or (get headers "Content-Type")
                    (get headers "content-type"))]
    (re-find #"application/edn" ct)))

(defn decode-edn-body [h]
  (fn [r]
    (if (edn-content-type? r)
      (try
        (h (assoc r :edn-body (-> r response-body edn/read-string)))
        (catch Exception e
          (if (re-find #"EOF while reading" (str e))
            {:status 400
             :body "Malformed EDN"}
            (throw e))))
      (h r))))

(defn stacktrace-el->clj [s]
  {:class-name (.getClassName s)
   :line-number (.getLineNumber s)
   :method-name (.getMethodName s)})

(defn exception->log-entry [ e]
  {:message (.getMessage e)
   :stacktrace (->> (.getStackTrace e)
                    (map stacktrace-el->clj))})

(defn wrap-exception-logging [h]
  (fn [r]
    (try
      (h r)
      (catch Exception e
        (println (exception->log-entry e))
        (throw e)))))

(defn enable-mw [h mw enabled?]
  (fn [r]
    (if enabled?
      ((mw h) r)
      (h r))))

(defn wrap-500-page [h]
  (fn [r]
    (try
      (h r)
      (catch Exception e
        (.printStackTrace e)
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (hiccup->html-string
                 (common/five-hundred (:user r)))}))))

(def routes
  (-> _routes
      promote-session-user
      decode-edn-body
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      (wrap-session {:store session-store})
      (wrap-file "resources/public" {:allow-symlinks? true})
      (wrap-file-info {"woff" "application/font-woff"
                       "otf" "font/opentype"
                       "eot" "application/vnd.ms-fontobject"
                       "svg" "image/svg+xml"
                       "ttf" "application/x-font-ttf"})
      wrap-long-caching
      (enable-mw
        wrap-exception-logging config/log-exceptions?)
      (enable-mw
        prone/wrap-exceptions config/debug-exceptions?)
      wrap-500-page))
