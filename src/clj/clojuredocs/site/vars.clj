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
  [:li
   [:pre body]])

(defn var-page [ns name]
  (fn [{:keys [user]}]
    (let [name (unmunge-name name)

          {:keys [arglists name ns doc runtimes] :as v}
          (mon/fetch-one :vars :where {:name name :ns ns})
          examples (examples-for v)]
      (common/$main
        {:body-class "var-page"
         :user user
         :content [:div.row
                   [:div.col-sm-12
                    [:h1 name]
                    [:h2 ns]
                    [:ul.runtimes
                     (for [r runtimes]
                       [:li (str r)])]
                    [:ul.arglists
                     (map #($arglist name %) arglists)]
                    [:div.docstring
                     [:pre (-> doc
                               (str/replace #"\n\s\s" "\n"))]
                     [:div.copyright
                      "&copy; Rich Hickey. All rights reserved."
                      " "
                      [:a {:href "http://www.eclipse.org/legal/epl-v10.html"}
                       "Eclipse Public License 1.0"]]]
                    [:h3 (count examples) " Examples"]
                    [:ul
                     (map $example examples)]]]}))))
