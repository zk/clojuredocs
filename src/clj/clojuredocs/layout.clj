(ns clojuredocs.layout
  (:require [hiccup.page :refer (html5)]
            [clojuredocs.config :as config]
            [clojuredocs.github :as gh]))

(def gh-auth-url (gh/auth-redirect-url
                   (merge config/gh-creds
                          {:redirect-uri (config/url "/gh-callback")})))

(defn $user-area [user]
  [:li.user-area
   [:img.avatar {:src (:avatar-url user)}]
   [:span.login  (:login user)]
   " | "
   [:a {:href "/logout"}
    "Log Out"]])

(defn main [{:keys [content title body-class user]}]
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
          (if user
            ($user-area user)
            [:li [:a {:href gh-auth-url} "Log In"]])]]]]]
     [:div.container
      [:div.row
       [:div.col-md-10.col-md-offset-1
        content]]]
     [:script {:type "text/javascript" :src "/cljs/goog/base.js"}]
     [:script {:type "text/javascript" :src "/cljs/clojuredocs.js"}]
     [:script {:type "text/javascript"} "goog.require(\"clojuredocs.main\");"]]]])
