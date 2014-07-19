(ns clojuredocs.site.styleguide
  (:require [clojuredocs.site.common :as common]
            [clojuredocs.site.vars :as vars-page]
            [clojuredocs.quickref :as quickref]))


(defn section [title & body]
  [:secton
   [:h2 title]
   body])

(defn $section [{:keys [title nav-target content]}]
  [:section {:id nav-target}
   [:h2 title]
   (vec (concat [:div.section-content] content))])

(defn $nav [sections]
  [:ul.sidenav {:data-sticky-offset "20"}
   [:li [:h3 [:a {:href "#"
                  :data-animate-scroll "true"} "Top"]]]
   (for [{:keys [nav-target title]} sections]
     [:li
      [:h3
       [:a {:href (str "#" nav-target)
            :data-animate-scroll "true"
            :data-animate-buffer "20"} title]]])])

(def sections
  [{:title "Common Elements"
    :nav-target "common-elements"
    :content
    [[:p "Namespace nav tree, nests namespaces to save on horizontal space. Namespaces are linked, non-namespace bridge parts (e.g. "
      [:code "clojure"]
      ", "
      [:code "java"]
      ") are unlinked."]
     [:div.example.checker-bg
      (common/$namespaces ["clojure.core"
                           "clojure.java.shell"
                           "clojure.test"
                           "clojure.test.junit"
                           "clojure.test.tap"
                           "clojure.zip"])]]}
   {:title "Quick Search"
    :nav-target "quick-search"
    :content
    [[:p "The main search widget on the home page. This should feel immediately accessible and get users where they want to go, fast."]
     [:div.example.checker-bg.quick-lookup]
     [:p "When loading autocomplete"]
     [:div.example.checker-bg.quick-lookup-loading]
     [:p "Landing search w/ autocomplete: heterogenous results linking to vars, namespaces, and concept pages."]
     [:div.example.checker-bg.quick-lookup-autocomplete]]}
   {:title "Examples"
    :nav-target "examples"
    :content
    [[:p "Null state for " [:code "clojure.core/map"]]
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
        "clojure.core" "map")]
     [:p "Adding an example:"]
     [:div.example.checker-bg.styleguide-add-example]]}

   (let [sphere '{:title "Simple Values",
                  :categories
                  ({:title "Regular Expressions",
                    :groups
                    ({:syms (re-pattern re-matcher), :title "Create"}
                     {:syms (re-find re-matches re-seq re-groups), :title "Use"})})}]
     {:title "Quick Reference"
      :nav-target "quickref"
      :content
      [[:div.example.checker-bg
        (quickref/$toc [sphere])]
       [:div.example.checker-bg
        (quickref/$sphere sphere)]]}
     {:title "Comments"
      :nav-target "comments"
      :content
      [[:div.example.checker-bg.add-comment-example]]})])

(defn index [{:keys [user]}]
  (common/$main
    {:body-class "styleguide-page"
     :user user
     :content
     [:div.row
      [:div.col-md-2
       ($nav sections)]
      [:div.col-md-10
       [:h1 "Styleguide"]
       [:p "Here you'll find various UI elements used on the ClojureDocs site. This styleguide is designed to help you see how changes will the vairous states of our UI elements when making changes."]
       (map $section sections)]]}))
