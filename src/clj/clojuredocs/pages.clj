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
             "User-agent: *\nDisallow: /dev"
             "User-agent: *\nDisallow: /")}))

(def logout-resp
  (-> (redirect "/")
      (assoc :session nil)))

(defn examples-styleguide-handler
  [{:keys [uri user]}]
  (common/$main
    {:body-class "examples-styleguide-page"
     :title "Examples Style Guide | ClojureDocs - Community-Powered Clojure Documentation and Examples"
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
     :title "Clojure's Core Library | ClojureDocs - Community-Powered Clojure Documentation and Examples"
     :user user
     :page-uri uri
     :mobile-nav [{:title "Core Library"
                   :links [[:a {:href "/core-library"} "Overview"]
                           [:a {:href "/core-library/vars"} "All The Vars"]]}
                  {:title "Namespaces"
                   :links (->> search/clojure-lib
                               :namespaces
                               (map (fn [{:keys [name]}]
                                      [:a {:href (str "/" name)} name])))}]
     :content
     [:div.row
      [:div.col-sm-2
       [:div.sidenav
        {:data-sticky-offset "20"}
        [:h5 "Core Library"]
        [:ul
         [:li [:a {:href "/core-library"} "Overview"]]
         [:li [:a {:href "/core-library/vars"} "All Vars"]]]
        (common/$library-nav search/clojure-lib)]]
      [:div.col-sm-10
       [:section.markdown
        (common/memo-markdown-file "src/md/core-library.md")]
       (for [{:keys [name]} search/searchable-nss]
         (when-let [content (common/memo-markdown-file (str "src/md/namespaces/" name ".md"))]
           [:section.markdown
            [:h2 [:a {:href (str "/" name)} name]]
            content]))]]}))


(defn group-vars [vars]
  (->> vars
       (group-by
         (fn [v]
           (let [char (-> v :name first str/lower-case)]
             (if (< (int (first char)) 97)
               "*^%"
               char))))
       (sort-by #(-> % first))
       (map (fn [[c vs]]
              {:heading c
               :vars vs}))))

(defn core-library-vars-handler [{:keys [user uri]}]
  (common/$main
    {:body-class "core-library-page"
     :title "All Functions, Macros, and Vars in Clojure's Core Library | ClojureDocs - Community-Powered Clojure Documentation and Examples"
     :page-uri uri
     :user user
     :mobile-nav
     [{:title "Core Library"
       :links [[:a {:href "/core-library"} "Overview"]
               [:a {:href "/core-library/vars"} "All The Vars"]]}
      {:title "Namespaces"
       :links (->> search/clojure-lib
                   :namespaces
                   (map (fn [{:keys [name]}]
                          [:a {:href (str "/" name)} name])))}]
     :content
     [:div.row
      [:div.col-sm-2
       [:div.sidenav
        {:data-sticky-offset "20"}
        [:h5 "Core Library"]
        [:ul
         [:li [:a {:href "/core-library"} "Overview"]]
         [:li [:a {:href "/core-library/vars"} "All Vars"]]]
        (common/$library-nav search/clojure-lib)]]
      [:div.col-sm-10
       [:h1 "All Vars in Clojure's Core Library"]
       (for [[ns vars] (->> search/clojure-lib
                            :vars
                            (group-by :ns)
                            (sort-by first))]
         [:div.var-namespace-group
          [:h2 [:a {:href (str  "/" ns)} ns]]
          (for [{:keys [heading vars]} (group-vars vars)]
            [:div.var-group
             [:h3 heading]
             [:ul.var-list
              (for [{:keys [ns name]} vars]
                [:li (util/$var-link ns name name)])]])])]]}))

(defn add-see-alsos [results]
  (let [sa-lookup (->> results
                       (map #(select-keys % [:ns :name :library-url]))
                       (map (fn [{:keys [ns name library-url] :as l}]
                              [l (->> (mon/fetch :see-alsos
                                        :where {:from-var.ns ns
                                                :from-var.name name
                                                :from-var.library-url library-url})
                                      (map (fn [{:keys [to-var]}]
                                             {:ns (:ns to-var)
                                              :name (:name to-var)
                                              :library-url (:library-url to-var)})))]))
                       (into {}))]
    (->> results
         (map #(assoc % :see-alsos (get sa-lookup (select-keys % [:ns :name :library-url])))))))

(defn add-examples-count [results]
  (let [examples-lookup (->> results
                             (map #(select-keys % [:ns :name :library-url]))
                             (map (fn [{:keys [ns name library-url] :as l}]
                                    [l (mon/fetch-count :examples
                                         :where {:var.ns ns
                                                 :var.name name
                                                 :var.library-url library-url
                                                 :deleted-at nil})]))
                             (into {}))]
    (->> results
         (map #(assoc % :examples-count (get examples-lookup (select-keys % [:ns :name :library-url])))))))

(defn var-search-handler [{:keys [params]}]
  {:headers {"Content-Type" "application/edn;charset=utf-8"}
   :body (pr-str (->> params
                      :query
                      search/query
                      (take 10)
                      add-see-alsos
                      add-examples-count))})

(defn ac-vars-handler [{:keys [params]}]
  {:headers {"Content-Type" "application/edn;charset=utf-8"}
   :body (pr-str (->> params
                      :query
                      search/query
                      (take 5)
                      (filter #(get #{"var" "function" "special-form" "macro"} (:type %)))))})

(defn expand-ns [ns]
  (let [pattern (->> (str/split ns #"\.")
                     (map #(str % "[^.]*"))
                     (interpose "\\.")
                     (apply str)
                     re-pattern)]
    (->> search/clojure-lib
         :namespaces
         (map :name)
         (filter #(re-find pattern %))
         first)))

(defn lookup-var [ns name]
  (mon/fetch-one :vars :where {:name name :ns ns}))

(defn lookup-var-expand [ns name]
  (or (search/lookup (str ns "/" name))
      (search/lookup (str (expand-ns ns) "/" name))))

(defn format-concept-title [concept]
  (->> (str/split concept #"-")
       (map str/capitalize)
       (interpose " ")
       (apply str)))

(defn concept-page-handler [concept]
  (fn [{:keys [user uri]}]
    (common/$main
      {:title (str (format-concept-title concept) " | ClojureDocs - Community-Powered Clojure Documentation and Examples")
       :page-uri uri
       :user user
       :content
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

(defn $arglist [name a]
  [:li.arglist
   (str "("
        (util/html-encode name)
        (when-not (empty? a) " ")
        a
        ")")])

(defn $argform [s]
  [:li.arglist s])

(defn search-page-handler [{:keys [params uri user]}]
  (let [query (or (:q params) "")
        html-query (util/html-encode query)
        limit 10
        page (or (try
                   (Integer/parseInt (:page params))
                   (catch NumberFormatException e
                     1))
                 1)
        offset (* (max (dec page) 0) limit)
        total-results (search/query query)
        results (->> total-results
                     (drop offset)
                     (take limit)
                     (map #(search/lookup (str (:ns %) "/" (:name %))))
                     add-see-alsos
                     add-examples-count)]
    (common/$main
      {:title (str "Search results for: " query " | ClojureDocs - Community-Powered Clojure Documentation and Examples")
       :page-uri uri
       :body-class "search-results-page"
       :user user
       :content
       [:div.row
        [:div.col-sm-12
         [:div.search-results-header
          [:h1 "Search results for query: " [:b html-query]]
          [:p (inc offset)
           " to "
           (min (+ offset limit) (count total-results))
           " of "
           (count total-results)
           " results. "
           [:span.search-controls.pull-right
            (if (> page 1)
              [:a {:href (str "/search?q=" query "&page=" (dec page))} "prev page"]
              "prev page")
            " | "
            (if (= limit (count results))
              [:a {:href (str "/search?q=" query "&page=" (inc page))} "next page"]
              "next page")]]]
         [:ul.search-results
          (for [{:keys [ns name doc see-alsos examples-count arglists forms]} results]
            (let [html-name (util/html-encode name)]
              [:li.search-result
               [:h2 (util/$var-link ns name html-name)]
               [:h3 ns]
               [:ul.arglists
                (if forms
                  (map #($argform %) forms)
                  (map #($arglist name %) arglists))]
               [:p (common/ellipsis doc 300)]
               [:div.meta-info
                (util/pluralize examples-count "example" "examples")
                (when-not (empty? see-alsos)
                  [:span.see-alsos
                   " &middot; "
                   "See also: "
                   (->> (for [sa see-alsos]
                          [:span.see-also
                           (util/$var-link
                             (:ns sa)
                             (:name sa)
                             [:span.ns (:ns sa) "/"] (-> sa :name util/html-encode))])
                        (interpose ", "))])]]))]
         [:div.search-controls
          (if (> page 1)
            [:a {:href (str "/search?q=" query "&page=" (dec page))} "prev page"]
            "prev page")
          " | "
          (if (= limit (count results))
            [:a {:href (str "/search?q=" query "&page=" (inc page))} "next page"]
            "next page")]]]})))

(defroutes routes
  (GET "/robots.txt" [] robots-resp)
  (GET "/logout" [] logout-resp)
  (GET "/examples-styleguide" [] examples-styleguide-handler)

  (GET "/core-library" [] core-library-handler)
  (GET "/core-library/vars" [] core-library-vars-handler)

  (GET "/concepts/:concept" [concept] (concept-page-handler concept))

  (GET "/search" [] search-page-handler)

  ;; Search Feedback
  (GET "/search-feedback" [] search-feedback/page-handler)
  (POST "/search-feedback" [] search-feedback/submit-feedback-handler)
  (GET "/search-feedback/success" [] search-feedback/success-handler)

  (GET "/ac-search" [] var-search-handler)
  (GET "/ac-vars" [] ac-vars-handler)

  (GET "/" [] intro/page-handler)
  (GET "/u/:login" [login] (user/page-handler login "github"))
  (GET "/uc/:login" [login] (user/page-handler login "clojuredocs"))

  ;; Account Migration
  (GET "/migrate-account" [] user/migrate-account-handler)
  (POST "/migrate-account/send-email" [] user/send-email-handler)
  (GET "/migrate-account/migrate/:migration-key" [migration-key]
    (user/migrate-account-migrate-handler migration-key))
  (POST "/migrate-account/migrate/:migration-key" [migration-key]
    (user/post-migrate-account-migrate-handler migration-key))

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
