(ns clojuredocs.pages.common
  (:require [clojure.string :as str]
            [clojuredocs.util :as util]
            [clojuredocs.config :as config]
            [clojuredocs.env :as env]
            [clojuredocs.github :as gh]))

(defn gh-auth-url [& [redirect-to-after-auth-url]]
  (let [redirect-url (str "/gh-callback" redirect-to-after-auth-url)]
    (gh/auth-redirect-url
      (merge config/gh-creds
             {:redirect-uri (config/url redirect-url)}))))


(defn $ga-script-tag [ga-tracking-id]
  (when ga-tracking-id
    [:script "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', '" ga-tracking-id "', 'auto');
  ga('send', 'pageview');"]))

(defn $user-area [user]
  [:li.user-area
   [:a {:href "/logout"}
    [:img.avatar {:src (:avatar-url user)}]
    " Log Out"]])

(defn $navbar [{:keys [user hide-search page-uri full-width?]}]
  [:header.navbar
   [:div
    {:class (if full-width? "container-fluid" "container")}
    [:div.row
     [:div
      {:class (if full-width?
                "col-md-12"
                "col-md-10 col-md-offset-1")}
      [:a.navbar-brand {:href "/"}
       [:i.fa.fa-rocket]
       "ClojureDocs"]
      [:button.btn.btn-default.navbar-btn.pull-right.mobile-menu
       [:i.fa.fa-bars]]
      [:ul.navbar-nav.nav.navbar-right.desktop-navbar-nav
       [:li [:a {:href "/core-library"} "Core Library"]]
       [:li [:a {:href "/quickref"} "Quick Reference"]]
       (if user
         ($user-area user)
         [:li
          [:a {:href (gh-auth-url page-uri)}
           [:i.fa.fa-github-square] "Log In"]])]
      (when-not hide-search
        [:div.quick-search-widget.navbar-right.navbar-form
         [:form.search
          {:autocomplete "off"}
          [:input.placeholder.form-control
           {:type "text"
            :name "query"
            :placeholder "Looking for? (ctrl-s)"
            :autocomplete "off"}]]])]]
    (when-not hide-search
      [:div.row
       [:div.col-md-10.col-md-offset-1
        [:div.ac-results-widget]]])]])

(defn $mobile-navbar-nav [{:keys [user page-uri mobile-nav]}]
  [:div.mobile-nav-menu
   [:section
    [:h4 [:i.fa.fa-rocket] "ClojureDocs"]
    [:ul.navbar-nav.mobile-navbar-nav.nav
     [:li [:a {:href "/core-library"} "Core Library"]]
     [:li [:a {:href "/quickref"} "Quick Reference"]]
     (if user
       ($user-area user)
       [:li
        [:a {:href (gh-auth-url page-uri)}
         [:i.fa.fa-github-square] "Log In"]])]]
   (for [{:keys [title links]} mobile-nav]
     [:section
      [:h4 title]
      [:ul.navbar-nav.mobile-navbar-nav.nav
       (for [link links]
         [:li link])]])])

(defn md5-path [path]
  (try
    (-> path slurp util/md5)
    (catch java.io.FileNotFoundException e
      nil)))

(def clojuredocs-script
  [:script {:src (str "/cljs/clojuredocs.js?"
                      (md5-path "resources/public/cljs/clojuredocs.js"))}])

(def app-link
  [:link {:rel :stylesheet
          :href (str "/css/app.css?"
                     (md5-path "resources/public/css/app.css"))}])

(def bootstrap-link
  [:link {:rel :stylesheet
          :href (str "/css/bootstrap.min.css?"
                     (md5-path "resources/public/css/bootstrap.min.css"))}])

(def font-awesome-link
  [:link {:rel :stylesheet
          :href (str "/css/font-awesome.min.css?"
                     (md5-path "resources/public/css/font-awesome.min.css"))}])

(defn $main [{:keys [page-uri content title body-class user page-data full-width?] :as opts}]
  [:html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, maximum-scale=1.0"}]
    [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
    [:meta {:name "apple-mobile-web-app-status-bar-style" :content "default"}]
    [:meta {:name "apple-mobile-web-app-title" :content "ClojureDocs"}]
    [:title (or title "Community-Powered Clojure Documentation and Examples | ClojureDocs")]
    font-awesome-link
    bootstrap-link
    app-link
    [:script "window.PAGE_DATA=" (util/to-json (pr-str page-data)) ";"]]
   [:body
    (when body-class
      {:class body-class})
    ($mobile-navbar-nav opts)
    [:div.mobile-nav-bar
     ($navbar opts)]
    [:div.sticky-wrapper.mobile-push-wrapper
     (when config/staging-banner?
       [:div.staging-banner
        "This is the ClojureDocs staging site, where you'll find all the neat things we're working on."])
     [:div.desktop-nav-bar
      ($navbar opts)]
     [:div
      {:class (if full-width?
                "container-fluid"
                "container")}
      [:div.row
       [:div
        {:class (if full-width?
                  "col-md-12"
                  "col-md-10 col-md-offset-1")}
        content]]]
     [:div.sticky-push]]
    [:footer
     [:div.container
      [:div.row
       [:div.col-sm-12
        [:div.divider
         "- ❦ -"]]]
      [:div.row
       [:div.ctas
        [:div.col-sm-6.left
         "Brought to you by "
         [:a {:href "https://twitter.com/heyzk"} "@heyzk"]
         ". "]
        [:div.col-sm-6.right
         [:iframe.gh-starred-count
          {:src "/github-btn.html?user=zk&repo=clojuredocs&type=watch&count=true"
           :allowtransparency "true"
           :frameborder "0"
           :scrolling "0"
           :width "80"
           :height "20"}]
         #_[:iframe {:src "/github-btn.html?user=zk&repo=clojuredocs&type=fork&count=true"
                     :allowtransparency "true"
                     :frameborder "0"
                     :scrolling "0"
                     :width "80"
                     :height "20"}]
         [:a.twitter-share-button {:href "https://twitter.com/share"
                                   :data-url "http://clojuredocs.org"
                                   :data-text "Community-powered docs and examples for #Clojure"
                                   :data-via "heyzk"}
          "Tweet"]]]
       [:script
        "!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');"]]]]
    ($ga-script-tag config/ga-tracking-id)
    (when (env/bool :cljs-dev)
      [:script {:src "/js/fastclick.min.js"}])
    (when (env/bool :cljs-dev)
      [:script {:src "/js/morpheus.min.js"}])
    (when (env/bool :cljs-dev)
      [:script {:src "/js/react.js"}])
    (when (env/bool :cljs-dev)
      [:script {:src "/js/marked.min.js"}])
    (when (env/bool :cljs-dev)
      [:script {:src "/cljs/goog/base.js"}])
    clojuredocs-script
    (when (env/bool :cljs-dev)
      [:script "goog.require(\"clojuredocs.main\");"])
    ;; mobile safari home screen mode
    [:script
     "if((\"standalone\" in window.navigator) && window.navigator.standalone){
var noddy, remotes = false;

document.addEventListener('click', function(event) {

noddy = event.target;

while(noddy.nodeName !== \"A\" && noddy.nodeName !== \"HTML\") {
noddy = noddy.parentNode;
}

if('href' in noddy && noddy.href.indexOf('http') !== -1 && (noddy.href.indexOf(document.location.host) !== -1 || remotes))
{
event.preventDefault();
document.location.href = noddy.href;
}

},false);
}"]]])

(defn $avatar [{:keys [email login avatar-url] :as user}]
  [:a {:href (str "/u/" login)}
   [:img.avatar
    {:src (or avatar-url
              (str "https://www.gravatar.com/avatar/"
                   (util/md5 email)
                   "?r=PG&s=32&default=identicon")) }]])

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
     [:h5 "Namespaces"]
     ($namespaces (map :name namespaces) current-ns)]))

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
     [:h5 "Recent"]
     [:ul
      (for [{:keys [text href]} recent]
        [:li [:a {:href href} (ellipsis text 10)]])]]))

(defn four-oh-four [{:keys [user]}]
  ($main
    {:body-class "error-page"
     :hide-search true
     :user user
     :content
     [:div.row
      [:div.col-sm-8.col-sm-offset-2
       [:h1 "404"]
       [:a.four-oh-four {:href "http://emareaf.deviantart.com/art/Rich-Hickey-321501046"}
        [:img.four-oh-four {:src "http://fc04.deviantart.net/fs70/f/2012/229/a/6/rich_hickey_by_emareaf-d5bevsm.png"}]]]]}))

(defn five-hundred [{:keys [user]}]
  ($main
    {:body-class "error-page"
     :hide-search true
     :user user
     :content
     [:div.row
      [:div.col-sm-8.col-sm-offset-2
       [:h1 "500"]
       [:a.four-oh-four {:href "http://emareaf.deviantart.com/art/Rich-Hickey-321501046"}
        [:img.four-oh-four {:src "http://fc04.deviantart.net/fs70/f/2012/229/a/6/rich_hickey_by_emareaf-d5bevsm.png"}]]]]}))

(defn memo-markdown-file [path]
  (try
    (-> path
        slurp
        util/markdown)
    (catch java.io.FileNotFoundException e
      nil)))

(defn prep-for-syntaxhighligher [s]
  (when s
    (-> s
        (str/replace #"<pre><code>" "<pre>")
        (str/replace #"</code></pre>" "</pre>")
        (str/replace #"<pre>" "<pre class=\"clojure\">"))))

(when config/cache-markdown?
  (def memo-markdown-file (memoize memo-markdown-file)))
