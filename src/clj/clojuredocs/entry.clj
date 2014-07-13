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
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE)]
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
            [clojure.pprint :refer (pprint)]))

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

(defroutes _routes
  (var site.intro/routes)
  (var site.gh-auth/routes)
  (var site.user/routes)
  (GET "/logout" [] (fn [r] (-> (redirect "/")
                                (assoc :session nil))))
  (GET "/quickref" [] quickref/index)
  (GET "/styleguide" [] styleguide/index)
  (GET "/ex/:id" [id] (site.vars/example-page id))


  ;; Redirect old urls
  (GET "/clojure_core/:ns/:name" [ns name]
    (fn [r]
      (let [name (->> name
                      util/cd-decode
                      util/cd-encode)]
        {:status 301
         :headers {"Location" (str "/" ns "/" name)}})))

  (GET "/:ns/:name" [ns name] (site.vars/var-page ns name))
  (GET "/:ns" [ns] (site.nss/index ns))
  (not-found (fn [r]
               (common/four-oh-four r))))

(def session-store
  (cookie-store
    {:key (env/str :session-key)
     :domain ".clojuredocs.org"}))

(defn promote-session-user [h]
  (fn [{:keys [session] :as r}]
    (h (assoc r :user (:user session)))))

(def routes
  (-> _routes
      promote-session-user
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      (wrap-session {:store session-store})
      (wrap-file "resources/public" {:allow-symlinks? true})
      wrap-file-info))
