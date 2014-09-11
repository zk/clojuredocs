(ns clojuredocs.canary
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [clojure.string :as str]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn $test-result [{:keys [title method path status expanded?] :as res} owner]
  (reify
    om/IRender
    (render [_]
      (let [status (or status :pending)]
        (sab/html
          [:div.test-result
           {:on-click #(om/transact! res :expanded? not)
            :class (name status)}
           title " " [:code (str method) " " path " - " (name status)]
           [:pre (pr-str res)]])))))

(defn $tests [test-group owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div
         (for [{:keys [title tests]} test-group]
           [:div
            [:h3 title]
            (om/build-all $test-result tests)])]))))

(def test-groups
  [{:title "Static Pages"
    :tests [{:title "Home"
             :method :get
             :path "/"}
            {:title "Quick Ref"
             :method :get
             :path "/quickref"}
            {:title "Namespace"
             :method :get
             :path "/clojure.core"}]}])

(defn run-test [{:keys [method path]}]
  (let [c (chan)]
    (ajax
      {:method method
       :path path
       :success (fn [d] (put! c d))
       :error (fn [r] (put! c r))})
    c))

(defn update-test-group [{:keys [tests] :as tg} test results]
  (assoc tg
    :tests
    (->> tests
         (map #(if (= test %)
                 (do
                   (prn test)
                   (assoc test :results results))
                 test)))))

(defn update-results! [tgs tg test results]
  (->> tgs
       (map #(if (= tg %)
               (update-test-group tg test results)
               tg))))

(defn run-test-groups [test-groups]
  (doseq [{:keys [tests] :as tg} @test-groups]
    (let [ch (async/to-chan tests)]
      (go
        (loop [test (<! ch)]
          (when test
            (let [result (<! (run-test test))]
              (swap! test-groups #(update-results! % tg test result))
              (recur (<! ch)))))))))

(defn init [$el]
  (let [tg (atom test-groups)]
    (om/root
      $tests
      tg
      {:target $el})
    (run-test-groups tg)))
