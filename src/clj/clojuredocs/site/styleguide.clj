(ns clojuredocs.site.styleguide
  (:require [clojuredocs.site.common :as common]
            [clojuredocs.site.vars :as vars-page]))

(defn section [title & body]
  [:secton
   [:h2 title]
   body])

(defn index [{:keys [user]}]
  (common/$main
    {:body-class "styleguide-page"
     :user user
     :content
     [:div
      [:h1 "Styleguide"]
      [:p "Here you'll find various UI elements used on the ClojureDocs site."]
      (section "Landing Search"
        [:p "The main search widget on the home page. This should feel immediately accessible and get users where they want to go, fast."]
        [:div.example.checker-bg.quick-lookup]
        [:p "Landing search w/ autocomplete: heterogenous results linking to vars, namespaces, and concept pages."]
        [:div.example.checker-bg.quick-lookup-autocomplete])
      (section "Examples"
        [:p "Null state for " [:code "clojure.core/map"]]
        [:div.example.checker-bg
         (vars-page/$examples [] "clojure.core" "map")]
        [:p "Populated w/ examples"]
        [:div.example.checker-bg
         (vars-page/$examples [{:body "user=> (map #(vector (first %) (* 2 (second %)))
            {:a 1 :b 2 :c 3})

([:a 2] [:b 4] [:c 6])

user=> (into {} *1)
{:a 2, :b 4, :c 6}"
                                :user {:email "zachary.kim@gmail.com"}
                                :history [{:user {:email "zachary.kim@gmail.com"}}]}]
           "clojure.core" "map")]
        [:p "Various example lengths"]
        [:div.example.checker-bg
         (vars-page/$examples [{:body "user=> (foo)"
                                :user {:email "zachary.kim@gmail.com"}
                                :history [{:user {:email "lee@writequit.org"}}
                                          {:user {:email "zachary.kim@gmail.com"}}]}
                               {:body "user=> (map #(vector (first %) (* 2 (second %)))
            {:a 1 :b 2 :c 3})

([:a 2] [:b 4] [:c 6])

user=> (into {} *1)
{:a 2, :b 4, :c 6}"
                                :user {:email "zachary.kim@gmail.com"}
                                :history [{:user {:email "masondesu@gmail.com"}}
                                          {:user {:email "lee@writequit.org"}}
                                          {:user {:email "zachary.kim@gmail.com"}}]}
                               {:body "user=> (map #(vector (first %) (* 2 (second %)))
            {:a 1 :b 2 :c 3})

([:a 2] [:b 4] [:c 6])

user=> (into {} *1)
{:a 2, :b 4, :c 6}"
                                :user {:email "zachary.kim@gmail.com"}
                                :history [{:user {:email "foo@barrrrrrr.com"}}
                                          {:user {:email "foo@barrrrrr.com"}}
                                          {:user {:email "foo@barrrrr.com"}}
                                          {:user {:email "foo@barrrr.com"}}
                                          {:user {:email "foo@barrr.com"}}
                                          {:user {:email "foo@barr.com"}}
                                          {:user {:email "foo@bar.com"}}
                                          {:user {:email "fickamanda@gmail.com"}}
                                          {:user {:email "brentdillingham@gmail.com"}}
                                          {:user {:email "masondesu@gmail.com"}}
                                          {:user {:email "lee@writequit.org"}}
                                          {:user {:email "zachary.kim@gmail.com"}}]}]
           "clojure.core" "map")])]}))
