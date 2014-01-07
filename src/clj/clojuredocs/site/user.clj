(ns clojuredocs.site.user
  (:require [clojuredocs.util :as util]
            [clojuredocs.site.common :as common]
            [somnium.congomongo :as mon]
            [compojure.core :refer (defroutes GET)]))

(defroutes routes
  (GET "/u/:login" [] (fn [{:keys [user]}]
                        (let [user {}]
                          (common/$main
                            {:user user
                             :content [:h1 "HELLO WORLD"]})))))
