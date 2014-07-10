(ns clojuredocs.site.common
  (:require [clojure.string :as str]
            [clojuredocs.util :as util]
            [clojuredocs.config :as config]
            [clojuredocs.env :as env]
            [clojuredocs.github :as gh]))

(def gh-auth-url (gh/auth-redirect-url
                   (merge config/gh-creds
                          {:redirect-uri (config/url "/gh-callback")})))

(defn $user-area [user]
  [:li.user-area
   [:img.avatar {:src (:avatar-url user)}]
   " | "
   [:a {:href "/logout"}
    "Log Out"]])

(defn $navbar [{:keys [user hide-search]}]
  [:header.navbar
   [:div.container
    [:div.row
     [:div.col-md-10.col-md-offset-1
      [:a.navbar-brand {:href "/"}
       [:i.fa.fa-rocket]
       "ClojureDocs"]
      [:ul.navbar-nav.nav.navbar-right.collapse.navbar-collapse
       [:li [:a {:href "/"} "Home"]]
       [:li [:a {:href "/quickref"} "Quick Reference"]]
       (if user
         ($user-area user)
         [:li
          [:a {:href gh-auth-url}
           [:i.fa.fa-github-square] "Log In"]])]
      (when-not hide-search
        [:div.quick-search-widget.navbar-right.navbar-form
         [:form.search
          {:autocomplete "off"}
          [:input.form-control {:type "text"
                                :name "query"
                                :placeholder "Looking for? (ctrl-s)"
                                :autocomplete "off"}]]])]]
    (when-not hide-search
      [:div.row
       [:div.col-md-10.col-md-offset-1
        [:div.ac-results-widget]]])]])

(defn $main [{:keys [content title body-class user page-data] :as opts}]
  [:html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, maximum-scale=1.0"}]
    [:title (or title "Community-Powered Clojure Documentation and Examples | ClojureDocs")]
    [:link {:rel :stylesheet :href "//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css"}]
    [:link {:rel :stylesheet :href "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css"}]
    [:link {:rel :stylesheet :href "//fonts.googleapis.com/css?family=Open+Sans:400" :type "text/css"}]
    [:link {:rel :stylesheet :href "/css/app.css"}]
    [:script "window.PAGE_DATA=" (util/to-json (pr-str page-data)) ";"]]
   [:body
    [:div.sticky-wrapper
     (when body-class
       {:class body-class})
     ($navbar opts)
     [:div.container
      [:div.row
       [:div.col-md-10.col-md-offset-1
        content]]]
     [:div.sticky-push]]
    [:footer
     [:div.divider
      "⤜ ❦ ⤛"]
     [:div.ctas
      "Brought to you by "
      [:a {:href "https://twitter.com/heyzk"} "@heyzk"]
      ". "
      "&nbsp; / "
      [:iframe {:src "/github-btn.html?user=zk&repo=clojuredocs&type=watch&count=true"
                :allowtransparency "true"
                :frameborder "0"
                :scrolling "0"
                :width "80"
                :height "20"}]
      [:iframe {:src "/github-btn.html?user=zk&repo=clojuredocs&type=fork&count=true"
                :allowtransparency "true"
                :frameborder "0"
                :scrolling "0"
                :width "80"
                :height "20"}]
      [:a.twitter-share-button {:href "https://twitter.com/share"
                                :data-url "http://clojuredocs.org"
                                :data-text "Community-powered docs and examples for #Clojure"
                                :data-via "heyzk"}
       "Tweet"]
      [:script
       "!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');"]]]
    (when (env/bool :cljs-dev)
      [:script {:src "http://fb.me/react-0.9.0.js"}])
    (when (env/bool :cljs-dev)
      [:script {:type "text/javascript" :src "/cljs/goog/base.js"}])
    [:script {:type "text/javascript" :src "/cljs/clojuredocs.js"}]
    (when (env/bool :cljs-dev)
      [:script {:type "text/javascript"} "goog.require(\"clojuredocs.main\");"])]])

(defn $avatar [{:keys [email login] :as user}]
  [:a {:href (str "/u/" login)}
   [:img.avatar {:src (str "https://www.gravatar.com/avatar/" (util/md5 email) "?r=PG&s=32&default=identicon") }]])


(defn group-levels [path ns-lookup current-ns ls]
  (when-not (empty? ls)
    (->> ls
         (group-by first)
         (map (fn [[k vs]]
                (let [path (str path (when path ".") k)
                      vs (map #(drop 1 %) vs)
                      vs (remove empty? vs)]
                  {:part k
                   :path path
                   :ns (get ns-lookup path)
                   :current? (= current-ns path)
                   :cs (group-levels path ns-lookup current-ns vs)})))
         (sort-by :part))))

(defn group-namespaces [nss & [current-ns]]
  (->> nss
       (map #(str/split % #"\."))
       (group-levels nil (set nss) current-ns)))

(defn $ns-tree [{:keys [part path ns cs current?]}]
  [:li
   [:span {:class (when current? "current")}
    (if ns
      [:a {:href (str "/" ns)} part]
      part)]
   (when-not (empty? cs)
     [:ul (map $ns-tree cs)])])

(defn $namespaces [namespaces & [current-ns]]
  (let [ns-trees (group-namespaces namespaces current-ns)]
    [:ul.ns-tree
     (map $ns-tree ns-trees)]))

(defn $library-nav [{:keys [name namespaces]} & [current-ns]]
  (when-not (empty? namespaces)
    [:div.library-nav
     [:h3 "Namespaces"]
     ($namespaces namespaces current-ns)]))

(defn ellipsis [s n]
  (cond
    (<= (count s) 3) s
    (> n (count s))  s
    :else (str (->> s
                    (take n)
                    (apply str))
               "...")))

(defn $recent [recent]
  (when-not (empty? recent)
    [:div.recent-pages
     [:h3 "Recent"]
     [:ul
      (for [{:keys [text href]} recent]
        [:li [:a {:href href} (ellipsis text 10)]])]]))
