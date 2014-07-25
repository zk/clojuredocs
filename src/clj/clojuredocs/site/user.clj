(ns clojuredocs.site.user
  (:require [clojuredocs.util :as util]
            [clojuredocs.site.common :as common]
            [somnium.congomongo :as mon]
            [compojure.core :refer (defroutes GET)]))

(defroutes routes
  (GET "/u/:login" [] (fn [{:keys [user uri]}]
                        (let [user {}]
                          (common/$main
                            {:user user
                             :page-uri uri
                             :content [:h1 "HELLO WORLD"]})))))
