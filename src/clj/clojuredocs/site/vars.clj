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

(defn $arglist [name a]
  [:li.arglist (str
                 "(" name (when-not (empty? a) " ") a ")")])

(defn $example-body [{:keys [body]}]
  [:div.example-body
   [:pre.raw-example {:class "brush: clojure"} body]])

(defn $example [{:keys [body _id history created-at updated-at] :as ex}]
  [:div.var-example
   [:div
    (let [users (->> history
                     (map :user)
                     distinct
                     reverse)
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

(defn source-url [{:keys [file line]}]
  (str "https://github.com/clojure/clojure/blob/clojure-1.5.1/src/clj/" file "#L" line))

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
   [:h3 (util/pluralize (count examples) "Example" "Examples")]
   (if (empty? examples)
     [:div.null-state
      "No examples for " ns "/" name ", "
      [:a {:href "#"} "add one"]
      "?"]
     (map $example examples))])

(defn var-page [ns name]
  (fn [{:keys [user session]}]
    (let [name (util/unmunge-name name)
          {:keys [arglists name ns doc runtimes added file] :as v}
          (lookup-var ns name)
          examples (examples-for v)
          see-alsos (see-alsos-for v)
          library (library-for v)
          recent (:recent session)]
      {:session (update-in session [:recent]
                  #(->> %
                        (concat [{:text name
                                  :href (str "/" ns "/" name)}])
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
                     [:div.col-sm-2
                      (common/$recent recent)
                      (common/$library-nav library ns)]
                     [:div.col-sm-10
                      [:div.row
                       [:div.col-sm-8
                        [:h1.var-name name]]
                       [:div.col-sm-4
                        [:div.var-meta
                         [:h2 [:a {:href (str "/" ns)} ns]]
                         "Available in "
                         (->> ["clj" "cljs" "clj.net"]
                              (interpose ", ")
                              (apply str))]]
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
                       [:div.add-example-widget]]
                      [:section
                       [:h3 "See Also"]
                       (if (empty? see-alsos)
                         [:div.null-state
                          "No see-alsos for " [:code name] ", "
                          [:a
                           {:href "#"} "add one"]
                          "?"]
                         [:div.row
                          (map $see-also see-alsos)])]]]]})})))

(defn $example-history-point [{:keys [user body created-at updated-at] :as ex}]
  [:div.row
   [:div.col-md-10
    [:div.example-code
     [:pre {:class "brush: clojure"}
      (-> body
          (str/replace #"<" "&lt;")
          (str/replace #">" "&gt;"))]]]
   [:div.col-md-2
    [:div.example-meta
     (common/$avatar user)
     [:div.created
      (util/timeago created-at) " ago."]]]])

(defn example-page [id]
  (fn [{:keys [user session]}]
    (let [{:keys [history name ns] :as ex}
          (mon/fetch-one :examples :where {:_id (util/bson-id id)})]
      (common/$main
         {:body-class "example-page"
          :user user
          :content [:div.row
                    [:div.col-md-12
                     [:h3 "Example History"]
                     [:p
                      "Example history for "
                      (util/$var-link ns name (str ns "/" name))
                      ", in order from newest to oldest. "
                      "The currrent version is highlighted in yellow."]
                     [:div.current-example
                      ($example ex)]
                     (->> history
                          reverse
                          (map $example-history-point))]]}))))
