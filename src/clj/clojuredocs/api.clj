(ns clojuredocs.api
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE ANY)]
            [clojuredocs.util :as util]))

(defroutes routes
  (ANY "/render-markdown" []
    (fn [r]
      (let [body (util/response-body r)]
        {:body (when body (util/markdown body))
         :headers {"Content-Type" "text/html;charset=utf-8"}}))))
