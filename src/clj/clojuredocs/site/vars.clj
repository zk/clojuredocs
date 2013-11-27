(ns clojuredocs.site.vars
  (:require [clojuredocs.layout :as layout]
            [clojuredocs.search :as search]
            [clojure.string :as str]))

(defn $arglist [name a]
  [:li.arglist (str
                 "("
                 (->> a
                      (cons name)
                      (apply list)
                      (interpose " ")
                      (apply str))
                 ")")])

(defn unmunge-name [s]
  (-> s
      (str/replace #"_dot" ".")
      (str/replace #"_div" "/")))

(defn var-page [ns name]
  (fn [{:keys [user]}]
    (let [name (unmunge-name name)
          {:keys [arglists name ns doc] :as v}
          (search/lookup (str ns "/" name))]
      (layout/main
        {:body-class "var-page"
         :user user
         :content [:div.row
                   [:div.col-sm-3
                    "TOC"]
                   [:div.col-sm-9
                    [:h1 name]
                    [:h2 ns]
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
                    [:pre (pr-str v)]]]}))))
