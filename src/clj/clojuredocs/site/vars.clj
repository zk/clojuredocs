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

(defn $arglist [name a]
  [:li.arglist (str
                 "(" name " " a ")")])

(defn unmunge-name [s]
  (-> s
      (str/replace #"_dot" ".")
      (str/replace #"_div" "/")
      (str/replace #"_qm" "?")))

(defn $avatar [{:keys [email login] :as user}]
  [:a {:href (str "/u/" login)}
   [:img.avatar {:src (str "https://www.gravatar.com/avatar/" (util/md5 email) "?r=PG&s=64&default=identicon") }]])

(defn $example-body [{:keys [body]}]
  [:div.example-code
   [:pre {:class "brush: clj"} body]])

(defn $example [{:keys [body _id history] :as ex}]
  [:div.row
   [:div.col-md-10
    [:a {:id (str "example_" _id)}]
    ($example-body ex)]
   [:div.col-md-2
    (let [users (->> history
                     (map :user)
                     distinct)]
      [:div.example-meta
       [:div.contributors
        (->> users
             (take 10)
             (map $avatar))]
       (when (> (count users) 10)
         [:div.contributors
          (count users) " contributors total."])
       [:div.created
        "Created 10 days ago."]
       [:div.last-updated
        "Last updated 5 days ago."]
       [:div.links
        [:a {:href (str "#example_" _id)}
         "link"]
        [:a {:href (str "/ex/" _id)}
         "history"]]])]])

(defn source-url [{:keys [file line]}]
  (str "https://github.com/clojure/clojure/blob/clojure-1.5.1/src/clj/" file "#L" line))

(defn var-page [ns name]
  (fn [{:keys [user]}]
    (let [name (unmunge-name name)

          {:keys [arglists name ns doc runtimes added file] :as v}
          (mon/fetch-one :vars :where {:name name :ns ns})
          examples (examples-for v)]
      (common/$main
        {:body-class "var-page"
         :user user
         :content [:div.row
                   [:div.col-sm-12
                    [:h1 name]
                    [:h2 ns]
                    [:ul.arglists
                     (map #($arglist name %) arglists)]
                    [:ul.runtimes
                     (for [r runtimes]
                       [:li (str r)])]
                    (when added
                      [:div.added
                       "Available since version " added])
                    (when file
                      [:div.source-code
                       [:a {:href (source-url v)} "Source"]])
                    [:div.docstring
                     [:pre (-> doc
                               (str/replace #"\n\s\s" "\n"))]
                     [:div.copyright
                      "&copy; Rich Hickey. All rights reserved."
                      " "
                      [:a {:href "http://www.eclipse.org/legal/epl-v10.html"}
                       "Eclipse Public License 1.0"]]]
                    [:pre (pr-str v)]
                    [:h3 (count examples) " Examples"]
                    (map $example examples)]]}))))

(defn $example-history-point [{:keys [user body] :as ex}]
  [:div.row
   [:div.col-md-10
    [:div.example-code
     [:pre (-> body
               (str/replace #"<" "&lt;")
               (str/replace #">" "&gt;"))]]]
   [:div.col-md-2
    "By " (:login user)]])

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
                     "Example history for id "
                     [:em id]
                     ", in order from oldest to newest. "
                     "This is an example for "
                     ns "/" name
                     ". The currrent version is highlighted in yellow."]
                    [:div.current-example
                     ($example ex)]
                    (->> history
                         reverse
                         (map $raw-example))

                    [:pre (pr-str ex)]]]}))))
