(ns clojuredocs.post-jobs
  (:require [dommy.core :as dommy :refer-macros [sel sel1]]
            [om.core :as om :include-macros true]
            [sablono.core :as sab :refer-macros [html]]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.util :as util]
            [clojuredocs.examples :as examples]
            [clojuredocs.see-alsos :as see-alsos]
            [clojuredocs.notes :as notes]
            [clojuredocs.anim :as anim]
            [clojuredocs.ajax :refer [ajax]]

            [nsfw.ops :as ops])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn $page []
  (reify
    om/IRender
    (render [_]
      [:h1 "HELLO WORLD"])))

(defn init-state [initial] {})

(defn init [body _])
