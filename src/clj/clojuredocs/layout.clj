(ns clojuredocs.layout
  (:require [hiccup.page :refer (html5)]))

(defn main [{:keys [content title]}]
  (html5
    [:head
     [:title (or title "Community-Powered Clojure Documentation and Examples | ClojureDocs")]
     [:link {:rel :stylesheet :href "/css/bootstrap.min.css"}]
     [:link {:rel :stylesheet :href "/css/font-awesome.min.css"}]
     [:link {:rel :stylesheet :href "/css/app.css"}]
     [:body
      [:header.navbar
       [:div.container
        [:div.row
         [:div.col-md-10.col-md-offset-1
          [:a.navbar-brand {:href "/"}
           [:i.icon-rocket]
           "ClojureDocs"]
          [:ul.navbar-nav.nav.pull-right
           [:li [:a {:href "/"} "Home"]]
           [:li [:a {:href "/quickref"} "Quick Reference"]]
           [:li [:a {:href "https://github.com/zk/clojuredocs"} "GitHub"]]]]]]]
      [:div.container
       [:div.row
        [:div.col-md-10.col-md-offset-1
         content]]]
      [:script {:type "text/javascript" :src "/cljs/clojuredocs.js"}]]]))
