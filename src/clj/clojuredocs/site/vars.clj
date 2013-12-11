(ns clojuredocs.site.vars
  (:require [clojure.string :as str]
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

(defn $example [{:keys [body]}]
  [:div.row
   [:div.col-md-10
    [:div.example-code
     [:pre {:class "brush: clj"} body]]]
   [:div.col-md-2
    [:div.example-meta
     [:div.contributors
      (repeat 4 [:div.fake-avatar])]
     [:div.created
      "Created 10 days ago."]
     [:div.last-updated
      "Last updated 5 days ago."]]]])

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
