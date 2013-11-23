(ns clojuredocs.layout
  (:require [hiccup.page :refer (html5)]))

(defn main [{:keys [content title body-class]}]
  [:html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, maximum-scale=1.0"}]
    [:title (or title "Community-Powered Clojure Documentation and Examples | ClojureDocs")]
    [:link {:rel :stylesheet :href "/css/bootstrap.min.css"}]
    [:link {:rel :stylesheet :href "/css/font-awesome.min.css"}]
    [:link {:rel :stylesheet :href "/css/app.css"}]
    [:body
     (when body-class
       {:class body-class})
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
     [:script {:type "text/javascript" :src "/cljs/goog/base.js"}]
     [:script {:type "text/javascript" :src "/cljs/clojuredocs.js"}]
     [:script {:type "text/javascript"} "goog.require(\"clojuredocs.main\");"]]]])
