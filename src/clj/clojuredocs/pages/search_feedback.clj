(ns clojuredocs.pages.search-feedback
  (:require [clojuredocs.util :as util]
            [somnium.congomongo :as mon]
            [clojuredocs.pages.common :as common]))

(defn page-handler [{:keys [params uri user]}]
  (common/$main
    {:body-class "search-feedback-page"
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-12
       [:h3 "Send us a note on how we can improve ClojureDocs."]
       [:p "We're sorry you couldn't find what you were looking for. If you leave us a note below with what you were looking for and how you tried to find it, we can use your feedback to make the site better."]
       [:div.search-feedback-widget
        {:data-query (:query params)}]]]}))

(defn submit-feedback-handler [{:keys [edn-body]}]
  (try
    (mon/insert! :search-feedback
      (assoc edn-body :created-at (util/now)))
    {:status 200
     :body "Ok!"}
    (catch Exception e
      (.printStackTrace e)
      {:body (str "Whoops, something went wrong. " (.getMessage e))
       :status 500})))


(defn success-handler [{:keys [user uri]}]
  (common/$main
    {:body-class "search-feedback-page"
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-12
       [:h3 "Thanks for your feedback"]
       [:p "Perhaps you can find what you're looking for using our "
        [:a {:href "/quickref"} "Clojure quick reference"]
        "."]]]}))
