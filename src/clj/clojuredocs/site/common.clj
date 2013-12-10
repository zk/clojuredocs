(ns clojuredocs.site.common
  (:require [clojuredocs.config :as config]
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

(defn $navbar [{:keys [user hide-search]}]
  [:header.navbar
   [:div.container
    [:div.row
     [:div.col-md-10.col-md-offset-1
      [:a.navbar-brand {:href "/"}
       [:i.icon-rocket]
       "ClojureDocs"]
      [:ul.navbar-nav.nav.navbar-right
       [:li [:a {:href "/"} "Home"]]
       [:li [:a {:href "/quickref"} "Quick Reference"]]
       (if user
         ($user-area user)
         [:li [:a {:href gh-auth-url} "Log In"]])]
      (when-not hide-search
        [:form.search.navbar-form.navbar-right
         {:method :get :action "/search" :autocomplete "off"}
         [:input.form-control {:type "text"
                               :name "query"
                               :placeholder "Quick Lookup (ctrl-s)"
                               :autocomplete "off"}]])]]
    (when-not hide-search
      [:div.row
       [:div.col-md-10.col-md-offset-1
        [:table.ac-results]]])]])

(defn $main [{:keys [content title body-class user] :as opts}]
  [:html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, maximum-scale=1.0"}]
    [:title (or title "Community-Powered Clojure Documentation and Examples | ClojureDocs")]
    [:link {:rel :stylesheet :href "/css/bootstrap.min.css"}]
    [:link {:rel :stylesheet :href "/css/font-awesome.min.css"}]
    [:link {:rel :stylesheet :href "/css/app.css"}]
    [:link {:rel :stylesheet :href "//fonts.googleapis.com/css?family=Open+Sans:400" :type "text/css"}]
    [:body
     (when body-class
       {:class body-class})
     ($navbar opts)
     [:div.container
      [:div.row
       [:div.col-sm-10.col-sm-offset-1
        content]]]
     [:script {:type "text/javascript" :src "/cljs/goog/base.js"}]
     [:script {:type "text/javascript" :src "/cljs/clojuredocs.js"}]
     [:script {:type "text/javascript"} "goog.require(\"clojuredocs.main\");"]]]])
