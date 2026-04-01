(ns clojuredocs.pages.common
  (:require [clojure.string :as str]
            [clojuredocs.util :as util]
            [nsfw.util]
            [clojuredocs.config :as config]
            [clojuredocs.env :as env]
            [clojuredocs.github :as gh]
            [clojuredocs.search :as search]))

(defn gh-auth-url [& [redirect-to-after-auth-url]]
  (let [redirect-url (str "/gh-callback" redirect-to-after-auth-url)]
    (gh/auth-redirect-url
      (merge config/gh-creds
             {:redirect-uri (config/url redirect-url)}))))


(defn $matomo-script-tag []
  [:script "var _paq = window._paq = window._paq || [];
   /* tracker methods like \"setCustomDimension\" should be called before \"trackPageView\" */
   _paq.push(['trackPageView']);
   _paq.push(['enableLinkTracking']);
   (function() {
                var u=\"https://cognitect.matomo.cloud/\";
                _paq.push(['setTrackerUrl', u+'matomo.php']);
                _paq.push(['setSiteId', '15']);
                var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
                g.async=true; g.src='https://cdn.matomo.cloud/cognitect.matomo.cloud/matomo.js'; s.parentNode.insertBefore(g,s);
                })();"])

(defn $user-area [user]
  [:li.user-area
   [:a {:href "/logout"}
    [:img.avatar {:src (:avatar-url user)}]
    " Log Out"]])

(defn $navbar [{:keys [user hide-search page-uri full-width? show-stars?]}]
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
      #_[:div.navbar-brand.clojure-version
         (:version search/clojure-lib)]
      [:button.btn.btn-default.navbar-btn.pull-right.mobile-menu
       [:i.fa.fa-bars]]
      [:ul.navbar-nav.nav.navbar-right.desktop-navbar-nav
       (when hide-search
         [:li
          [:div.navbar-brand.clojure-version
           [:a {:href (:gh-tag-url search/clojure-lib)}
            (:version search/clojure-lib)]]])
       #_[:li [:a {:href "/jobs"} "Jobs"]]
       [:li [:a {:href "/core-library"} "Core Library"]]
       [:li [:a {:href "/quickref"} "Quick Ref"]]
       (if user
         ($user-area user)
         [:li
          [:a {:href (gh-auth-url page-uri)}
           [:i.fa.fa-github-square] "Log In"]])
       (when show-stars?
         [:li
          [:iframe.gh-starred-count
           {:src "/github-btn.html?user=nubank&repo=clojuredocs&type=watch&count=true"
            :allowtransparency "true"
            :frameborder "0"
            :scrolling "0"
            :width "100"
            :height "20"}]])]
      (when-not hide-search
        [:div.nav-search-widget.navbar-right.navbar-form
         [:form.search
          {:autocomplete "off"
           :action "/search"
           :method :get}
          [:input.placeholder.form-control
           {:type "text"
            :name "q"
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
     [:li
      [:a {:href "/core-library"} "Core Library"
       [:span.clojure-version "(1.12.4)"]]]
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
    (catch java.io.FileNotFoundException _e
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

(def opensearch-link
  [:link {:rel "search"
          :href "/opensearch.xml"
          :type "application/opensearchdescription+xml"
          :title "ClojureDocs"}])

(defn $main [{:keys [content
                     title
                     body-class
                     page-data
                     meta
                     full-width?] :as opts}]
  [:html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, maximum-scale=1.0"}]
    [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
    [:meta {:name "apple-mobile-web-app-status-bar-style" :content "default"}]
    [:meta {:name "apple-mobile-web-app-title" :content "ClojureDocs"}]
    [:meta {:name "google-site-verification" :content "XjzqkjEPtcgtLjhnqAvtnVSeveEccs-O_unFGGlbk4g"}]
    (->> meta
         (map (fn [[k v]]
                [:meta {:name k :content v}])))
    [:title (or title "Community-Powered Clojure Documentation and Examples | ClojureDocs")]
    opensearch-link
    font-awesome-link
    bootstrap-link
    app-link
    [:script "// <![CDATA[\nwindow.PAGE_DATA=" (util/to-json (nsfw.util/to-transit page-data)) ";\n//]]>"]]
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
         "- ❦ -"]]]]]
    (when (env/bool :cljs-dev)
      [:script {:src "/js/fastclick.min.js"}])
    (when (env/bool :cljs-dev)
      [:script {:src "/js/morpheus.min.js"}])
    (when (env/bool :cljs-dev)
      [:script {:src "/js/marked.min.js"}])
    clojuredocs-script
    ($matomo-script-tag)
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

(defn $avatar [{:keys [email login avatar-url]}]
  [:a {:href (str "/u/" login)}
   [:img.avatar
    {:src (or avatar-url
              (str "https://www.gravatar.com/avatar/"
                   (util/md5 email)
                   "?r=PG&s=32&default=identicon"))}]])

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

(defn $ns-tree [{:keys [part ns cs current?]}]
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

(defn $library-nav [{:keys [namespaces]} & [current-ns]]
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
    (catch java.io.FileNotFoundException _e
      nil)))

(defn prep-for-syntaxhighligher [s]
  (when s
    (-> s
        (str/replace #"<pre><code>" "<pre>")
        (str/replace #"</code></pre>" "</pre>")
        (str/replace #"<pre>" "<pre class=\"clojure\">"))))

(when config/cache-markdown?
  (def memo-markdown-file (memoize memo-markdown-file)))
