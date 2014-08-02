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
            [clojuredocs.site.common :as common]
            [clojuredocs.quickref :as quickref]
            [clojuredocs.site.intro :as site.intro]
            [clojuredocs.site.gh-auth :as site.gh-auth]
            [clojuredocs.site.vars :as site.vars]
            [clojuredocs.site.user :as site.user]
            [clojuredocs.site.nss :as site.nss]
            [clojuredocs.site.styleguide :as styleguide]
            [clojure.pprint :refer (pprint)]
            [clojuredocs.api :as api]
            [somnium.congomongo :as mon]
            [clojure.edn :as edn]))

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

(defroutes old-url-redirects
  (GET "/clojure_core/:ns/:name" [ns name] (redirect-to-var ns name))
  (GET "/clojure_core/:version/:ns/:name" [ns name] (redirect-to-var ns name))
  (GET "/quickref/*" [] {:status 301 :headers {"Location" "/quickref"}})
  (GET "/clojure_core" [] {:status 301 :headers {"Location" "/"}}))

(defn expand-ns [ns]
  (:name (mon/fetch-one :namespaces
           :where {:name (->> (str/split ns #"\.")
                              (map #(str % "[^.]*"))
                              (interpose "\\.")
                              (apply str)
                              re-pattern)})))

(defn lookup-var [ns name]
  (mon/fetch-one :vars :where {:name name :ns ns}))

(defn lookup-var-expand [ns name]
  (or (lookup-var ns name)
      (lookup-var (expand-ns ns) name)))

(defroutes _routes
  (GET "/robots.txt" []
    (fn [r]
      {:headers {"Content-Type" "text/plain"}
       :body (if config/allow-robots?
               "User-agent: *\nAllow: /"
               "User-agent: *\nDisallow: /")}))
  (var site.intro/routes)
  (var site.gh-auth/routes)
  (var site.user/routes)
  (context "/api" [] api/_routes)

  (GET "/logout" [] (fn [r] (-> (redirect "/")
                                (assoc :session nil))))
  (GET "/quickref" [] quickref/index)
  (GET "/styleguide" [] styleguide/index)
  (GET "/examples-styleguide" []
    (fn [{:keys [uri user]}]
      (common/$main
        {:body-class "examples-styleguide-page"
         :user user
         :page-uri uri
         :content
         [:div.row
          [:div.col-md-10.col-md-offset-1.examples-styleguide-content
           (-> "src/md/examples-styleguide.md"
               slurp
               util/markdown)]]})))
  (GET "/ex/:id" [id] (site.vars/example-page id))

    ;; Redirect old urls
  (var old-url-redirects)

  (GET "/:ns/:name" [ns name] (site.vars/var-page ns name))
  (GET "/:ns" [ns] (site.nss/index ns))

  (GET "/:ns/:name" [ns name]
    (fn [r]
      (let [{:keys [ns name]} (lookup-var-expand ns name)]
        {:status 307
         :headers {"Location" (str "/" ns "/" (util/cd-encode name))}})))

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

(defn decode-edn-body [h]
  (fn [r]
    (if (= "application/edn" (get-in r [:headers "content-type"]))
      (try
        (h (assoc r :edn-body (-> r response-body edn/read-string)))
        (catch Exception e
          {:status 400
           :body "Malformed edn"}))
      (h r))))

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
      wrap-long-caching))
