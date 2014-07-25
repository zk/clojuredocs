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
  (mon/fetch-one :libraries :where {:namespaces ns}))

(defn comments-for [{:keys [ns name library-url]}]
  (mon/fetch :var-comments
    :where {:var.ns ns :var.name name :var.library-url library-url}
    :sort {:created-at 1}))

(defn $arglist [name a]
  [:li.arglist (str
                 "(" name (when-not (empty? a) " ") a ")")])

(defn $example-body [{:keys [body]}]
  [:div.example-body
   [:pre.raw-example {:class "brush: clojure"} body]])

(defn $example [{:keys [body _id user history created-at updated-at] :as ex}]
  [:div.var-example
   [:div
    (let [users (distinct
                  (concat
                    [user]
                    (->> history
                         (map :user)
                         reverse)))
          num-to-show 7]
      [:div.example-meta
       [:div.contributors
        (->> users
             (take num-to-show)
             (map common/$avatar))
        (when (> (count users) 10)
          [:div.contributors
           "+ "
           (- (count users) num-to-show)
           " more"])]

       [:div.links
        [:a {:href (str "#example_" _id)}
         "permalink"]
        " / "
        [:a {:href (str "/ex/" _id)}
         "history"]]])]
   [:div
    [:a {:id (str "example_" _id)}]
    ($example-body ex)]])

(defn source-url [{:keys [file line ns]}]
  (when (= "clojure.core" ns)
    (str "https://github.com/clojure/clojure/blob/clojure-1.6.0/src/clj/" file "#L" line)))

(defn $see-also [{:keys [ns name created-at doc user] :as sa}]
  [:div.col-sm-6.see-also
   [:div
    (util/$var-link ns name
      [:span.ns ns "/"]
      [:span.name name])]
   [:p
    (->> doc
         (take 100)
         (apply str))
    (when (> (count doc) 100)
      "...")]
   [:div.meta
    "Added by " [:a {:href (str "/u/" (:login user))} (:login user)]]])

(defn lookup-var [ns name]
  (or (mon/fetch-one :vars :where {:name name :ns ns})
      (search/lookup (str ns "/" name))))

(defn $examples [examples ns name]
  [:div.var-examples
   [:h5 (util/pluralize (count examples) "Example" "Examples")]
   (if (empty? examples)
     [:div.null-state
      "No examples for " ns "/" name ", "
      [:a {:href "#"} "add one"]
      "?"]
     (map $example examples))])

(defn $comment [{:keys [body user created-at]}]
  [:div.comment
   [:div.comment-meta
    "By "
    (common/$avatar user)
    " "
    (:login user)
    ", "
    (util/timeago created-at)
    " ago."]
   [:div.comment-body
    (-> (util/markdown body)
        (str/replace #"<pre><code>" "<pre class=\"brush: clojure\">")
        (str/replace #"</code></pre>" "</pre>"))]])

(defn $comments [comments name]
  [:div.var-comments
   [:h5 (util/pluralize (count comments) "Comment" "Comments")]
   [:div
    (if (empty? comments)
      [:div.null-state "No comments for " [:code name]]
      [:ul
       (for [c comments]
         ($comment c))])]
   [:div.add-comment-widget]])

(defn var-page [ns name]
  (let [name (util/cd-decode name)
        {:keys [arglists name ns doc runtimes added file] :as v} (lookup-var ns name)]
    (fn [{:keys [user session]}]
      (when v
        (let [examples (examples-for v)
              see-alsos (see-alsos-for v)
              library (library-for v)
              recent (:recent session)
              comments (comments-for v)]
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
              :page-data {:examples (map #(assoc % :_id (str (:_id %))) examples)
                          :var (assoc v :_id (str (:_id v)))}
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
                           [:div.examples-widget
                            ($examples examples ns name)]
                           [:div.add-example-widget
                            {:data-var (str (:ns v) "/" (:name v))}]]
                          [:section
                           [:h5 "See Also"]
                           (if (empty? see-alsos)
                             [:div.null-state
                              "No see-alsos for " [:code name]]
                             [:div.row
                              (map $see-also see-alsos)])
                           [:div.add-see-also-widget]]
                          [:section
                           ($comments comments name)]]]]})})))))

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
  (fn [{:keys [user session]}]
    (let [{:keys [history name ns] :as ex}
          (mon/fetch-one :examples :where {:_id (util/bson-id id)})]
      (common/$main
         {:body-class "example-page"
          :user user
          :content [:div.row
                    [:div.col-md-12
                     [:p
                      "Example history for "
                      (util/$var-link ns name (str ns "/" name))
                      ", in order from newest to oldest. "
                      "The currrent version is outlined in yellow."]
                     [:div.current-example
                      ($example ex)]
                     (->> history
                          reverse
                          (map $example-history-point))]]}))))
