(ns clojuredocs.pages
  (:require [clojure.string :as str]
            [compojure.core :refer (defroutes GET POST)]
            [ring.util.response :refer (redirect)]
            [somnium.congomongo :as mon]
            [clojuredocs.util :as util]
            [clojuredocs.config :as config]
            [clojuredocs.search :as search]
            [clojuredocs.search.static :as search.static]
            [clojuredocs.pages.common :as common]
            [clojuredocs.pages.search-feedback :as search-feedback]
            [clojuredocs.pages.intro :as intro]
            [clojuredocs.pages.user :as user]
            [clojuredocs.pages.dev :as dev]
            [clojuredocs.pages.gh-auth :as gh-auth]
            [clojuredocs.pages.quickref :as quickref]
            [clojuredocs.pages.vars :as vars]
            [clojuredocs.pages.nss :as nss]))

(def robots-resp
  (fn [r]
    {:headers {"Content-Type" "text/plain"}
     :body (if config/allow-robots?
             "User-agent: *\nAllow: /"
             "User-agent: *\nDisallow: /")}))

(def logout-resp
  (-> (redirect "/")
      (assoc :session nil)))

(defn examples-styleguide-handler
  [{:keys [uri user]}]
  (common/$main
    {:body-class "examples-styleguide-page"
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-10.col-md-offset-1.examples-styleguide-content
       (-> "src/md/examples-styleguide.md"
           slurp
           util/markdown)]]}))

(defn core-library-handler [{:keys [user uri]}]
  (common/$main
    {:body-class "core-library-page"
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-sm-2
       [:div.sidenav
        {:data-sticky-offset "20"}
        (common/$library-nav search/clojure-lib)]]
      [:div.col-sm-10
       [:section.markdown
        (common/memo-markdown-file "src/md/core-library.md")]
       (for [{:keys [name]} search/searchable-nss]
         (when-let [content (common/memo-markdown-file (str "src/md/namespaces/" name ".md"))]
           [:section.markdown
            [:h2 [:a {:href (str "/" name)} name]]
            content]))]]}))

(defn add-see-alsos [results]
  (let [sa-lookup (->> results
                       (map #(select-keys % [:ns :name]))
                       (map (fn [{:keys [ns name] :as l}]
                              [l (->> (mon/fetch-one :see-alsos :where {:var.ns ns :var.name name})
                                      :refs
                                      (map (fn [{:keys [ns name]}]
                                             {:ns ns
                                              :name name
                                              :href (str "/" ns "/" name)})))]))
                       (into {}))]
    (->> results
         (map #(assoc % :see-alsos (get sa-lookup (select-keys % [:ns :name])))))))

(defn add-examples-count [results]
  (let [examples-lookup (->> results
                             (map #(select-keys % [:ns :name :library-url]))
                             (map (fn [{:keys [ns name library-url] :as l}]
                                    [l (mon/fetch-count :examples :where {:var.ns ns
                                                                          :var.name name
                                                                          :var.library-url library-url})]))
                             (into {}))]
    (->> results
         (map #(assoc % :examples-count (get examples-lookup (select-keys % [:ns :name :library-url])))))))


(defn var-search-handler [{:keys [params]}]
  {:headers {"Content-Type" "application/edn"}
   :body (pr-str (->> params
                      :query
                      search/query
                      add-see-alsos
                      add-examples-count))})

(defn ac-vars-handler [{:keys [params]}]
  {:headers {"Content-Type" "application/edn"}
   :body (pr-str (->> params
                      :query
                      search/query
                      (filter #(get #{"var" "function" "special-form" "macro"} (:type %)))))})

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

(defn concept-page-handler [concept]
  (fn [r]
    (common/$main
      {:content
       [:div.row
        [:div.col-sm-2
         [:div.sidenav
          {:data-sticky-offset "30"}
          [:h5 "Concepts"]
          [:ul
           (for [{:keys [name href]} search.static/concept-pages]
             [:li [:a {:href href} name]])]]]
        [:div.col-sm-10
         [:div.markdown
          (-> (str "src/md/concepts/" concept ".md")
              common/memo-markdown-file
              common/prep-for-syntaxhighligher)]]]})))

(defroutes routes
  (GET "/robots.txt" [] robots-resp)
  (GET "/logout" [] logout-resp)
  (GET "/examples-styleguide" [] examples-styleguide-handler)
  (GET "/core-library" [] core-library-handler)

  (GET "/concepts/:concept" [concept] (concept-page-handler concept))

  ;; Search Feedback
  (GET "/search-feedback" [] search-feedback/page-handler)
  (POST "/search-feedback" [] search-feedback/submit-feedback-handler)
  (GET "/search-feedback/success" [] search-feedback/success-handler)

  (GET "/search" [] var-search-handler)
  (GET "/ac-vars" [] ac-vars-handler)
  (GET "/" [] intro/page-handler)
  (GET "/u/:login" [login] (user/page-handler login "github"))
  (GET "/uc/:login" [login] (user/page-handler login "clojuredocs"))

  ;; Dev Stuff
  (GET "/dev/styleguide" [] dev/styleguide-handler)
  (GET "/dev/styleguide/search" [] dev/search-styleguide-handler)
  (GET "/dev/styleguide/examples" [] dev/examples-styleguide-handler)
  (GET "/dev/styleguide/inspector" [] dev/styleguide-inspector-handler)
  (GET "/dev/search-perf" [] dev/perf-handler)
  (GET "/dev/canary" [] dev/canary-tests-handler)
  (GET "/dev/api" [] dev/api-docs-handler)

  (GET "/gh-callback*" {{path :*} :route-params} (gh-auth/callback-handler path))
  (GET "/quickref" [] quickref/page-handler)
  (GET "/ex/:id" [id] (vars/example-handler id))

  (GET "/:ns" [ns] (nss/page-handler ns))

  (GET "/:ns/:name" [ns name] (vars/var-page-handler ns name))
  (GET "/:ns/:name" [ns name] ;; ns unmunging catch
    (fn [r]
      (let [{:keys [ns name] :as v} (lookup-var-expand ns name)]
        (when v
          {:status 307
           :headers {"Location" (str "/" ns "/" (util/cd-encode name))}})))))
