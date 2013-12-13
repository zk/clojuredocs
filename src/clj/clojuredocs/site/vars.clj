(ns clojuredocs.site.vars
  (:require [clojuredocs.util :as util]
            [clojure.string :as str]
            [somnium.congomongo :as mon]
            [clojuredocs.search :as search]
            [clojuredocs.site.common :as common]))

(defn examples-for [{:keys [ns name]}]
  (mon/fetch :examples :where {:name name
                               :ns ns
                               :library-url "https://github.com/clojure/clojure"}))

(defn see-alsos-for [{:keys [ns name]}]
  (->> (mon/fetch-one :see-alsos :where {:name name
                                         :ns ns
                                         :library-url "https://github.com/clojure/clojure"})
       :vars))

(defn $arglist [name a]
  [:li.arglist (str
                 "(" name (when-not (empty? a) " ") a ")")])

(defn $example-body [{:keys [body]}]
  [:div.example-code
   [:pre {:class "brush: clj"} body]])

(defn $example [{:keys [body _id history created-at updated-at] :as ex}]
  [:div.row
   [:div.col-md-10
    [:a {:id (str "example_" _id)}]
    ($example-body ex)]
   [:div.col-md-2
    (let [users (->> history
                     (map :user)
                     distinct
                     reverse)]
      [:div.example-meta
       [:div.contributors
        (->> users
             (take 10)
             (map common/$avatar))]
       (when (> (count users) 10)
         [:div.contributors
          (count users) " contributors total."])
       [:div.created
        "Created " (util/timeago created-at) " ago."]
       (when-not (= created-at updated-at)
         [:div.last-updated
          "Updated " (util/timeago updated-at) " ago."])
       [:div.links
        [:a {:href (str "#example_" _id)}
         "link"]
        " / "
        [:a {:href (str "/ex/" _id)}
         "history"]]])]])

(defn source-url [{:keys [file line]}]
  (str "https://github.com/clojure/clojure/blob/clojure-1.5.1/src/clj/" file "#L" line))

(defn $see-also [{:keys [ns name created-at doc] :as sa}]
  [:div.see-also
   [:div
    ns "/" name]
   [:div
    (->> doc
         (take 50)
         (apply str))]])

(defn var-page [ns name]
  (fn [{:keys [user]}]
    (let [name (util/unmunge-name name)
          {:keys [arglists name ns doc runtimes added file] :as v}
          (mon/fetch-one :vars :where {:name name :ns ns})
          examples (examples-for v)
          see-alsos (see-alsos-for v)]
      (common/$main
        {:body-class "var-page"
         :user user
         :content [:div
                   [:div.row
                    [:div.col-sm-4
                     [:section
                      [:h1 name]
                      [:h2 ns]]]
                    [:div.col-sm-4
                     [:section
                      [:ul.arglists
                       (map #($arglist name %) arglists)]]]
                    [:div.col-sm-4
                     [:section.var-meta
                      "Available in "
                      (->> ["clj" "cljs" "clj.net"]
                           (interpose ", ")
                           (apply str))
                      (when file
                        [:div.source-code
                         [:a {:href (source-url v)} "Source"]])]]]
                   [:div.row
                    [:div.col-sm-12
                     [:div.docstring
                      [:pre (-> doc
                                (str/replace #"\n\s\s" "\n"))]
                      [:div.copyright
                       "&copy; Rich Hickey. All rights reserved."
                       " "
                       [:a {:href "http://www.eclipse.org/legal/epl-v10.html"}
                        "Eclipse Public License 1.0"]]]
                     [:h3 (count examples) " Examples"]
                     (map $example examples)
                     [:h3 "See Alsos"]
                     (map $see-also see-alsos)]]]}))))

(defn $example-history-point [{:keys [user body created-at updated-at] :as ex}]
  [:div.row
   [:div.col-md-10
    [:div.example-code
     [:pre (-> body
               (str/replace #"<" "&lt;")
               (str/replace #">" "&gt;"))]]]
   [:div.col-md-2
    [:div.example-meta
     (common/$avatar user)
     [:div.created
      (util/timeago created-at) " ago."]]]])

(defn example-page [id]
  (fn [{:keys [user]}]
    (let [{:keys [history name ns] :as ex}
          (mon/fetch-one :examples :where {:_id (util/bson-id id)})]
      (common/$main
        {:body-class "example-page"
         :user user
         :content [:div.row
                   [:div.col-md-12
                    [:h3 "Example History"]
                    [:p
                     "Example history for example "
                     [:em id]
                     ", in order from oldest to newest. "
                     "This is an example for "
                     (util/$var-link ns name (str ns "/" name))
                     ". The currrent version is highlighted in yellow."]
                    [:div.current-example
                     ($example ex)]
                    (->> history
                         reverse
                         (map $example-history-point))
                    [:pre (pr-str ex)]]]}))))
