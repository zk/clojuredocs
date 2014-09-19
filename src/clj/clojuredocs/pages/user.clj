(ns clojuredocs.pages.user
  (:require [clojuredocs.util :as util]
            [clojuredocs.pages.common :as common]
            [somnium.congomongo :as mon]
            [compojure.core :refer (defroutes GET)]))

(defn page-handler [login account-source]
  (let [{:keys [login account-source] :as user}
        (mon/fetch-one :users :where {:login login :account-source account-source})

        examples-authored-count
        (mon/fetch-count :examples :where {:author.login login
                                           :author.account-source account-source})]
    (fn [r]
      (common/$main
        {:user (:user r)
         :page-uri (:uri r)
         :body-class "user-page"
         :content [:div.row
                   [:div.col-sm-10.col-sm-offset-1
                    [:div.row
                     [:div.col-sm-4
                      [:span.user-avatar
                       (util/$avatar user {:size 200})]]
                     [:div.col-sm-8
                      [:h1 login]
                      [:p "User " [:b login] " has authored " examples-authored-count " examples."]]]]]}))))

