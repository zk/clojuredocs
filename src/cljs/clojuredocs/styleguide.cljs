(ns clojuredocs.styleguide
  (:require [clojuredocs.widgets :as widgets]
            [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]))

(enable-console-print!)

(def init
  [[:div.quick-lookup]
   (fn [$el]
     (om/root
       widgets/quick-lookup
       {}
       {:target $el}))

   [:div.quick-lookup-autocomplete]
   (fn [$el]
     (om/root
       widgets/quick-lookup
       {}
       {:target $el
        :init-state {:autocomplete [{:type :function
                                     :ns "clojure.core"
                                     :name "map"
                                     :doc "Returns a lazy sequence consisting of the result of applying f to the set of first items of each coll, followed by applying f to the set
of second items in each coll, until any one of the colls is
exhausted. Any remaining items in other colls are ignored. Function
f should accept number-of-colls arguments."}
                                    {:type :ns
                                     :ns "clojure.core"
                                     :doc "Fundamental library of the Clojure language"}
                                    {:type :page
                                     :title "Getting Started"
                                     :desc "Where to go to get started with Clojure. Provides a host of information on the language, core concepts, tutorials, books, and videos to help you learn Clojure."}]}}))

   [:div.quick-lookup-loading]
   (fn [$el]
     (om/root
       widgets/quick-lookup
       {}
       {:target $el
        :init-state {:loading? true}}))

   [:div.styleguide-add-example]
   (fn [$el]
     (om/root
       widgets/add-example
       {}
       {:target $el :init-state {:expanded? true}}))

   [:div.add-comment-example]
   (fn [$el]
     (om/root
       widgets/$add-comment
       {}
       {:target $el
        :init-state {:expanded? true}}))])
