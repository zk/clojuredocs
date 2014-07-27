(ns clojuredocs.site.vars
  (:require [clojuredocs.util :as util]
            [clojure.string :as str]
            [somnium.congomongo :as mon]
            [clojuredocs.search :as search]
            [clojuredocs.site.common :as common]
            [hiccup.core :as hc]))

(defn ellipsis [s n]
  (cond
    (<= (count s) 3) s
    (> n (count s))  s
    :else (str (->> s
                    (take n)
                    (apply str))
               "...")))

(defn examples-for [{:keys [ns name]}]
  (mon/fetch :examples :where {:name name
                               :ns ns
                               :library-url "https://github.com/clojure/clojure"}))

(defn see-alsos-for [{:keys [ns name]}]
  (->> (mon/fetch-one :see-alsos :where {:name name
                                         :ns ns
                                         :library-url "https://github.com/clojure/clojure"})
       :vars))

(defn library-for [{:keys [ns]}]
  search/clojure-lib)

(defn notes-for [{:keys [ns name library-url]}]
  (mon/fetch :var-notes
    :where {:var.ns ns :var.name name :var.library-url library-url}
    :sort {:created-at 1}))

(defn $arglist [name a]
  [:li.arglist (str
                 "(" name (when-not (empty? a) " ") a ")")])

(defn source-url [{:keys [file line ns]}]
  (when (= "clojure.core" ns)
    (str "https://github.com/clojure/clojure/blob/clojure-1.6.0/src/clj/" file "#L" line)))

(defn lookup-var [ns name]
  (search/lookup (str ns "/" name)))

(defn $note [{:keys [body user created-at]}]
  [:div.note
   [:div.note-meta
    "By "
    (common/$avatar user)
    " "
    (:login user)
    ", "
    (util/timeago created-at)
    " ago."]
   [:div.note-body
    (-> (util/markdown body)
        (str/replace #"<pre><code>" "<pre class=\"brush: clojure\">")
        (str/replace #"</code></pre>" "</pre>"))]])

(defn $notes [notes name]
  [:div.var-notes
   [:h5 (util/pluralize (count notes) "Note" "Notes")]
   [:div
    (if (empty? notes)
      [:div.null-state "No notes for " [:code name]]
      [:ul
       (for [n notes]
         ($note n))])]
   [:div.add-note-widget]])


(defn clean-id [{:keys [_id] :as m}]
  (assoc m :_id (str _id)))

(defn clean-example [{:keys [_id user history] :as m}]
  (-> m
      (update-in [:user] dissoc :email)
      (update-in [:_id] str)))

(defn clean-see-also [m]
  (-> m
      (update-in [:user] dissoc :email)
      (update-in [:_id] str)))

(defn var-page [ns name]
  (let [name (util/cd-decode name)
        {:keys [arglists name ns doc runtimes added file] :as v} (lookup-var ns name)]
    (fn [{:keys [user session uri]}]
      (when v
        (let [examples (examples-for v)
              see-alsos (see-alsos-for v)
              library (library-for v)
              recent (:recent session)
              notes (notes-for v)]
          {:session (update-in session [:recent]
                      #(->> %
                            (concat [{:text name
                                      :href (str "/" ns "/" (util/cd-encode name))}])
                            distinct
                            (filter :text)
                            (take 4)))
           :body
           (common/$main
             {:body-class "var-page"
              :page-data {:examples (map clean-example examples)
                          :var v
                          :notes (map clean-id notes)
                          :see-alsos (map clean-see-also see-alsos)
                          :user (select-keys user [:login :avatar-url])}
              :page-uri uri
              :user user
              :content [:div
                        [:div.row
                         [:div.col-sm-2.sidenav
                          (common/$recent recent)
                          (common/$library-nav library ns)]
                         [:div.col-sm-10
                          [:div.row
                           [:div.col-sm-8
                            [:h1.var-name name]]
                           [:div.col-sm-4
                            [:div.var-meta
                             [:h4 [:a {:href (str "/" ns)} ns]]
                             (if added
                               [:span "Available since " added]
                               [:span "Available in 1.6"])
                             (when-let [su (source-url v)]
                               [:span.source-link
                                " ("
                                [:a {:href su} "source"]
                                ") "])]]
                           [:div.col-sm-12
                            [:section
                             [:ul.arglists
                              (map #($arglist name %) arglists)]]]]
                          [:section
                           [:div.docstring
                            (if doc
                              [:pre (-> doc
                                        (str/replace #"\n\s\s" "\n"))]
                              [:div.null-state "No Doc"])
                            (when doc
                              [:div.copyright
                               "&copy; Rich Hickey. All rights reserved."
                               " "
                               [:a {:href "http://www.eclipse.org/legal/epl-v10.html"}
                                "Eclipse Public License 1.0"]])]]
                          [:section
                           [:div.examples-widget]]
                          [:section
                           [:div.see-alsos-widget]]
                          [:section
                           [:div.notes-widget]]]]]})})))))

(defn $example-body [{:keys [body]}]
  [:div.example-body
   [:pre.raw-example {:class "brush: clojure"} body]])

(defn $example-history-point [{:keys [user body created-at updated-at] :as ex}]
  [:div.var-example
   [:div
    (let [num-to-show 7]
      [:div.example-meta
       [:div.contributors
        "Created by &nbsp;"
        (common/$avatar user)
        ""
        (util/timeago created-at) " ago."]
       [:div.links
        ]])]
   [:div ($example-body ex)]])

(defn example-page [id]
  (fn [{:keys [user session uri]}]
    (let [{:keys [history name ns] :as ex}
          (mon/fetch-one :examples :where {:_id (util/bson-id id)})]
      (common/$main
         {:body-class "example-page"
          :user user
          :page-uri uri
          :content [:div.row
                    [:div.col-md-12
                     [:p
                      "Example history for "
                      (util/$var-link ns name (str ns "/" name))
                      ", in order from newest to oldest. "
                      "The currrent version is outlined in yellow."]
                     #_[:div.current-example
                      ($example ex)]
                     (->> history
                          reverse
                          (map $example-history-point))]]}))))
