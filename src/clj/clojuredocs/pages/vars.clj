(ns clojuredocs.pages.vars
  (:require [clojuredocs.util :as util]
            [clojure.string :as str]
            [somnium.congomongo :as mon]
            [clojuredocs.search :as search]
            [clojuredocs.pages.common :as common]
            [clojuredocs.data :as data]
            [hiccup.core :as hc]))

(defn ellipsis [s n]
  (cond
    (<= (count s) 3) s
    (> n (count s))  s
    :else (str (->> s
                    (take n)
                    (apply str))
               "...")))

(defn library-for [{:keys [ns]}]
  search/clojure-lib)

(defn $arglist [name a]
  [:li.arglist (str
                 "(" name (when-not (empty? a) " ") a ")")])

(defn see-alsos-for [{:keys [ns name library-url]}]
  (->> (mon/fetch :see-alsos
         :where {:from-var.ns ns
                 :from-var.name name
                 :from-var.library-url library-url})
       (map (fn [{:keys [to-var] :as sa}]
              (assoc sa :doc (-> (str (:ns to-var) "/" (:name to-var)) search/lookup :doc))))))

(defn source-url [{:keys [file line ns]}]
  (when (= "clojure.core" ns)
    (str "https://github.com/clojure/clojure/blob/clojure-1.6.0/src/clj/" file "#L" line)))

(defn lookup-var [ns name]
  (search/lookup (str ns "/" name)))

(defn $note [{:keys [body user created-at]}]
  [:div.note
   [:div.note-meta
    "By "
    (util/$avatar user)
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

(defn $number-badge [num]
  [:span.badge num])

(defn $var-header [{:keys [ns name added arglists] :as v}]
  [:div.row.var-header
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
      (map #($arglist name %) arglists)]]]])

(defn var-page-handler [ns name]
  (let [name (util/cd-decode name)
        {:keys [arglists name ns doc runtimes added file] :as v} (lookup-var ns name)]
    (fn [{:keys [user session uri]}]
      (when v
        (let [examples (data/find-examples-for v)
              see-alsos (->> v
                             see-alsos-for
                             (map #(assoc % :can-delete? (util/is-author? user %))))
              library (library-for v)
              recent (:recent session)
              notes (->> v
                         data/find-notes-for
                         (map #(let [author? (util/is-author? user %)]
                                 (-> %
                                     (assoc :can-delete? author?)
                                     (assoc :can-edit? author?)))))]
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
              :title (str name " - " ns " | ClojureDocs")
              :page-data {:examples (mapv clean-example examples)
                          :var v
                          :notes (vec (map clean-id notes))
                          :see-alsos (vec (map clean-see-also see-alsos))
                          :user (when user (select-keys user [:login :avatar-url :account-source]))}
              :page-uri uri
              :user user
              :mobile-nav [{:title "Nav"
                            :links [[:a {:href "#"
                                         :data-animate-scroll "true"
                                         :data-animate-buffer "70"}
                                     "Top"]
                                    [:a {:href "#examples"
                                         :data-animate-scroll "true"
                                         :data-animate-buffer "70"}
                                     "Examples "
                                     [:span.examples-count
                                      ($number-badge (count examples))]]
                                    [:a {:href "#see-also"
                                         :data-animate-scroll "true"
                                         :data-animate-buffer "70"}
                                     "See Also " ($number-badge (count see-alsos))]
                                    (when (> (count notes) 0)
                                      [:a {:href "#notes"
                                           :data-animate-scroll "true"
                                           :data-animate-buffer "70"}
                                       "Notes " ($number-badge (count notes))])]}]
              :content [:div
                        [:div.row
                         [:div.col-sm-2.sidenav
                          [:div.desktop-side-nav {:data-sticky-offset "10"}
                           [:div.var-page-nav]
                           (common/$library-nav library ns)]]
                         [:div.col-sm-10
                          ($var-header v)
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
                           [:div.examples-widget {:id "examples"}]]
                          [:section
                           [:div.see-alsos-widget {:id "see-also"}]]
                          [:section
                           [:div.notes-widget {:id "notes"}]]]]]})})))))

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
        (util/$avatar user)
        ""
        (util/timeago created-at) " ago."]
       [:div.links
        ]])]
   [:div ($example-body ex)]])

(defn example-handler [id]
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
