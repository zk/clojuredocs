(ns clojuredocs.pages.user
  (:require [clojuredocs.util :as util]
            [clojuredocs.pages.common :as common]
            [somnium.congomongo :as mon]
            [compojure.core :refer (defroutes GET)]))

(defn page-handler [{:keys [user uri]}]
  (let [user {}]
    (common/$main
      {:user user
       :page-uri uri
       :content [:h1 "HELLO WORLD"]})))
