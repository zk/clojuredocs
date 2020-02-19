(ns clojuredocs.pages.quickref
  (:require [clojure.string :as str]
            [clojuredocs.util :as util]
            [clojuredocs.search :as search]
            [clojuredocs.pages.common :as common]
            [clojuredocs.pages.quickref.static :as static]))

(defn title->id [k]
  (-> k
      name
      str/lower-case
      (str/replace #"[^a-z0-9]" "-")
      (str/replace #"-+" "-")))

(defn $group [{:keys [title syms]} parent-title]
  [:div.group
   [:div.quickref-header.clearfix
    [:h4 title]
    [:h4.header-reference
     (when parent-title
       parent-title)
     (when parent-title
       " > ")
     title]]
   [:dl.dl-horizontal
    (mapcat #(vector
               [:div.dl-row
                [:dt (util/$var-link "clojure.core" (str %) (str %))]
                [:dd (->> (str "clojure.core/" %)
                          search/lookup-vars
                          :doc
                          (take 110)
                          (apply str))
                 #_[:span.examples-count.pull-right
                  "1 ex."]]])
      syms)]])

(defn $category [{:keys [title groups]} parent-title]
  [:div.category
   [:div.category-header.clearfix
    [:h3 {:id (title->id title)} title]
    [:h3.header-reference parent-title]]
   (map #($group % title) groups)])

(defn $sphere [{:keys [title categories]}]
  [:div.sphere
   [:div.sphere-header
    [:h2 {:id (title->id title)} title]]
   (map #($category % title) categories)])

(defn $toc-category [{:keys [title]}]
  [:li [:a {:href (str "#" (title->id title))
            :data-animate-scroll "true"
            :data-animate-buffer "10"}
        title]])

(defn $toc-sphere [{:keys [title categories]}]
  [:div.toc-sphere
   [:h5 [:a {:href (str "#" (title->id title))
             :data-animate-scroll "true"
             :data-animate-buffer "10"} title]]
   [:ul
    (map $toc-category categories)]])

(defn $toc [quickref-data]
  (let [toc-groups (partition-all 2 quickref-data)]
    [:div.toc.clearfix
     [:h5 "Table of Contents"]
     [:h6 [:a {:href "#"
               :data-animate-scroll "true"
               :data-animate-buffer "10"} "Top"]]
     (for [tg toc-groups]
       [:div
        (map $toc-sphere tg)])]))

(defn mobile-nav [quickref-data]
  {:title "Table of Contents"
   :links
   (->> quickref-data
        (map (fn [{:keys [title categories]}]
               [:a
                {:href (str "#" (title->id title))
                 :data-animate-scroll "true"
                 :data-animate-buffer "60"}
                [:div.quickref-mobile-toc
                 [:h5 title]
                 [:span.categories
                  (->> categories
                       (map :title)
                       (interpose ", ")
                       (apply str))]]])))})

(defn page-handler [{:keys [user uri]}]
  (common/$main
    {:body-class "quickref-page"
     :title "Clojure Quick Reference | ClojureDocs - Community-Powered Clojure Documentation and Examples"
     :user user
     :page-uri uri
     :mobile-nav [(mobile-nav static/quickref-data)]
     :content
     [:div
      [:div.row
       [:div.col-sm-3.sidenav.toc-sidenav
        [:div.page-toc
         {:data-sticky-offset "10"}
         ($toc static/quickref-data)]]
       [:div.col-sm-9
        [:h1 "Quickref for Clojure Core"]
        [:p
         "Adapted from Johannes Friestad's excellent quick ref."]
        (map $sphere static/quickref-data)]]]}))
