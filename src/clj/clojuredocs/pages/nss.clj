(ns clojuredocs.pages.nss
  (:require [clojuredocs.config :as config]
            [somnium.congomongo :as mon]
            [clojuredocs.util :as util]
            [clojuredocs.pages.common :as common]
            [clojuredocs.search :as search]
            [clojure.string :as str]))

(defn library-for [ns]
  search/clojure-lib)

(defn namespace-for [ns]
  (->> search/clojure-lib
       :namespaces
       (filter #(= ns (:name %)))
       first))

(defn vars-for [ns]
  (->> search/clojure-lib
       :vars
       (filter #(= ns (:ns %)))))

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

(defn $var-group [{:keys [heading vars]}]
  [:div.var-group
   [:h4.heading heading]
   (vec
     (concat
       [:dl.dl-horizontal]
       (->> vars
            (map (fn [{:keys [ns name doc]}]
                   (let [name (-> name
                                   (str/replace #"<" "&lt;")
                                   (str/replace #">" "&gt;"))]
                     [:div.dl-row
                      [:dt.name
                       (util/$var-link ns name name)]
                      (if doc
                        [:dd.doc doc]
                        [:dd.no-doc "no doc"])]))))))])

(defn page-handler [ns-str]
  (fn [{:keys [user uri] :as r}]
    (let [lib (library-for ns-str)
          ns (namespace-for ns-str)
          vars (sort-by #(-> % :name str/lower-case) (vars-for ns-str))]
      (when ns
        (common/$main
          {:body-class "ns-page"
           :user user
           :title (str (:name ns) " namespace | ClojureDocs - Community-Powered Clojure Documentation and Examples")
           :page-uri uri
           :mobile-nav [{:title "Namespaces"
                         :links (->> lib
                                     :namespaces
                                     (map (fn [{:keys [name]}]
                                            [:a {:href (str "/" name)} name])))}]
           :content [:div
                     [:div.row
                      [:div.col-sm-2.sidenav
                       [:div
                        {:data-sticky-offset "20"}
                        (common/$library-nav lib)]]
                      [:div.col-sm-10
                       [:h1 ns-str]
                       [:section.markdown
                        (when (:doc ns)
                          [:pre.doc (:doc ns)])
                        (common/memo-markdown-file (str "src/md/namespaces/" ns-str ".md"))]
                       [:section
                        [:h5 "Vars in " ns-str]
                        (->> vars
                             group-vars
                             (map $var-group))
                        #_[:table {:class "ns-table"}
                           ]]]]]})))))
