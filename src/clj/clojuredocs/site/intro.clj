(ns clojuredocs.site.intro
  (:require [compojure.core :refer (defroutes GET)]
            [clojuredocs.search :as search]
            [clojuredocs.site.common :as common]))

(def $index
  [:div
   [:div.row
    [:div.col-md-12
     [:section
      [:h1 "ClojureDocs is a community-powered documentation and examples repository for the " [:a {:href "http://clojure.org"} "Clojure programming language"] "."]]
     [:section
      [:form.search {:method :get :action "/search" :autocomplete "off"}
       [:input.form-control {:type "text"
                             :name "query"
                             :placeholder "Looking for?"
                             :autofocus "autofocus"
                             :autocomplete "off"}]]
      [:table.ac-results]]]]
   [:div.row
    [:div.col-md-6
     [:section
      [:h3 "Getting started with ClojureDocs"]
      [:p "Finding the right tool for the job can be tough, so we've outlined a few ways to go about your search below."]
      [:ul
       [:li [:i.icon-search] "Use the search box above to find what you're looking for."]
       [:li [:i.icon-map-marker] "Take a look at the Clojure Core quickref, which displays Clojure vars grouped by category."]
       [:li [:i.icon-book] "Browse an alphabetical list of vars defined in Clojure Core or Contrib."]]]
     [:section
      [:h3 "Getting started with Clojure"]
      [:p "It's no secret that wrapping your head around Clojure can be tough (it took me three tries!)."]
      [:p "The good news is learning Clojure and the concepts it espouses are getting easier every day. You will be transformed on the other side, so stick with it."]
      [:p ""]]
     [:section
      [:h3 "Contribute to ClojureDocs"]
      [:p "We need your help to make ClojureDocs a great community resource. Here are a couple of ways you can contribute."]
      [:ul
       [:li
        [:h4 [:i.icon-comment-alt] "Give Feedback"]
        [:p "Please " [:a {:href "https://github.com/zk/clojuredocs/issues"} "open a ticket"] " if you have an idea of how we can improve ClojureDocs."]]
       [:li
        [:h4 [:i.icon-indent-right] "Add an Example"]
        [:p "Sharing your knowledge with fellow Clojurists is easy:"]
        [:p "First, take a look at the examples style guide, and then add an example for your favorite var (or pick one from the list)."]
        [:p "In addition to examples, you also have the ability to add 'see also' references between vars."]]]]]
    [:div.col-md-5.col-md-offset-1
     [:h3 "Top Contributors"]]]])

(defroutes routes
  (GET "/" []
    (fn [{:keys [ user]}]
      (-> {:content $index
           :body-class "intro-page"
           :hide-search true
           :user user}
          common/$main)))

  (GET "/search" []
    (fn [{:keys [params]}]
      {:headers {"Content-Type" "application/edn"}
       :body (pr-str (search/query (:query params)))})))
