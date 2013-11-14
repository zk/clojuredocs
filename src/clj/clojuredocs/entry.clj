(ns clojuredocs.entry
  (:use [ring.middleware
         file
         file-info
         session
         params
         nested-params
         multipart-params
         keyword-params]
        [ring.middleware.session.cookie :only (cookie-store)])
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE)]
            [clojuredocs.env :as env]
            [clojuredocs.layout :as layout]))

(defn html-resp [body]
  {:headers {"Content-Type" "text/html;charset=utf-8"}
   :body body})


(def $index
  [:div
   [:div.row
    [:div.col-md-12
     [:div.intro
      [:h1 "ClojureDocs is a community-powered documentation and examples repository for the " [:a {:href "http://clojure.org"} "Clojure programming language"] "."]]
     [:form.search {:method :get :action "/search"}
      [:input.form-control {:type "text"
                            :name "query"
                            :placeholder "What do you need help with?"
                            :autofocus "autofocus"}]]]]
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

(defroutes _routes
  (GET "/" [] (fn [r]
                (-> {:content $index}
                    layout/main
                    html-resp)))
  (GET "/search" [] (fn [{:keys [params]}] {:body (pr-str params)})))

(def session-store
  (cookie-store
    {:key (env/str :session-key "abcdefg")
     :domain ".clojuredocs.org"}))

(def routes
  (-> _routes
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      (wrap-session {:store session-store})
      (wrap-file "resources/public" {:allow-symlinks? true})
      wrap-file-info))
