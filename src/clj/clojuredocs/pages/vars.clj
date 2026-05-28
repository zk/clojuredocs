(ns clojuredocs.pages.vars
  (:require [clojuredocs.util :as util]
            [clojure.string :as str]
            [somnium.congomongo :as mon]
            [clojuredocs.search :as search]
            [clojuredocs.search.compat :as compat]
            [clojuredocs.pages.common :as common]
            [clojuredocs.data :as data]
            [clojuredocs.config :as config]
            [cheshire.core :as json]
            [ring.util.codec :as codec]
            [hiccup.core :as hc]))

(defn ellipsis [s n]
  (cond
    (<= (count s) 3) s
    (> n (count s))  s
    :else (str (->> s
                    (take n)
                    (apply str))
               "...")))

(defn truncate-docstring [doc max-len]
  (if (or (nil? doc) (<= (count doc) max-len))
    doc
    (let [truncated (subs doc 0 max-len)
          last-space (str/last-index-of truncated " ")]
      (if last-space
        (str (subs truncated 0 last-space) "\u2026")
        (str truncated "\u2026")))))

(defn library-for [{:keys [ns]}]
  search/clojure-lib)

(defn $arglist [name a]
  [:li.arglist
   (str "("
        (util/html-encode name)
        (when-not (empty? a) " ")
        (util/html-encode a)
        ")")])

(defn $argform [s]
  [:li.arglist (util/html-encode s)])

(defn see-alsos-for [{:keys [ns name library-url]}]
  (->> (mon/fetch :see-alsos
         :where {:from-var.ns ns
                 :from-var.name name
                 :from-var.library-url library-url})
       (map (fn [{:keys [to-var] :as sa}]
         (let [ns-name (str (:ns to-var) "/" (:name to-var))
               looked-up-var (search/lookup ns-name)]
           (if (nil? looked-up-var) nil
               (assoc sa :doc (:doc looked-up-var))))))
       (remove nil?)))

;; Namespaces in the clojure/clojure repository (excludes external repos)
(def clojure-repo-namespaces
  #{"clojure.core"
    "clojure.core.protocols"
    "clojure.core.reducers"
    "clojure.core.server"
    "clojure.data"
    "clojure.datafy"
    "clojure.edn"
    "clojure.inspector"
    "clojure.instant"
    "clojure.java.browse"
    "clojure.java.io"
    "clojure.java.javadoc"
    "clojure.java.process"
    "clojure.java.shell"
    "clojure.main"
    "clojure.math"
    "clojure.pprint"
    "clojure.reflect"
    "clojure.repl"
    "clojure.repl.deps"
    "clojure.set"
    "clojure.stacktrace"
    "clojure.string"
    "clojure.template"
    "clojure.test"
    "clojure.test.junit"
    "clojure.test.tap"
    "clojure.walk"
    "clojure.xml"
    "clojure.zip"})

(defn source-url [{:keys [file line ns] :as var}]
  (when (and (contains? clojure-repo-namespaces ns) file)
    (str (:source-base-url search/clojure-lib) "/src/clj/" file "#L" line)))

(defn lookup-var [ns name]
  (search/lookup (str ns "/" name)))

(defn $note [{:keys [body user created-at]}]
  [:div.note
   [:div.note-meta
    "By "
    (util/$avatar user)
    " "
    (:login user)
    ", "
    (util/timeago created-at)
    " ago."]
   [:div.note-body
    (-> (util/markdown body)
        (str/replace #"<pre><code>" "<pre class=\"brush: clojure\">")
        (str/replace #"</code></pre>" "</pre>"))]])

(defn $notes [notes name]
  [:div.var-notes
   [:h5 (util/pluralize (count notes) "Note" "Notes")]
   [:div
    (if (empty? notes)
      [:div.null-state "No notes for " [:code name]]
      [:ul
       (for [n notes]
         ($note n))])]
   [:div.add-note-widget]])


(defn clean-id [{:keys [_id] :as m}]
  (assoc m :_id (str _id)))

(defn clean-example [{:keys [_id user history] :as m}]
  (-> m
      (update-in [:user] dissoc :email)
      (update-in [:_id] str)))

(defn clean-see-also [m]
  (-> m
      (update-in [:user] dissoc :email)
      (update-in [:_id] str)))

(defn $number-badge [num]
  [:span.badge num])

(def dialect-info
  {:clj  {:label "Clojure/JVM" :img "/img/clojure-logo.png"}
   :cljs {:label "ClojureScript" :img "/img/cljs-logo.svg"}
   :bb   {:label "babashka" :img "/img/babashka-logo.svg"}})

(defn $dialect-badges [ns-str name-str]
  (when-let [dialects (compat/dialects-for ns-str name-str)]
    [:span.dialect-badges
     (for [k [:clj :cljs :bb]
           :let [{:keys [label img]} (dialect-info k)
                 supported? (contains? dialects k)]]
       [:img {:src img
              :alt label
              :width 20
              :height 20
              :title (if supported?
                       (str "Available in " label)
                       (str "Not available in " label))
              :class (str "dialect-badge"
                          (when-not supported? " unsupported"))}])]))

(defn $implementation-badge [v]
  (when (search/impl-var? v)
    [:span.implementation-badge "Implementation detail"]))

(defn $implementation-notice [v]
  (when (search/impl-var? v)
    [:div.implementation-notice
     "This var is an implementation detail and is not part of the public API. "
     "It may change or be removed without notice."]))

(defn $var-header [{:keys [ns name added arglists forms] :as v}]
  [:div.row.var-header
   [:div.col-sm-8
    [:h1.var-name (util/html-encode name)]]
   [:div.col-sm-4
    [:div.var-meta
     [:h4 [:a {:href (str "/" ns)} ns]]
     (when added
       [:span "Available since " added])
     (when-let [su (source-url v)]
       [:span.source-link
        " ("
        [:a {:href su} "source"]
        ") "])
     ($dialect-badges ns name)
     ($implementation-badge v)]]
   [:div.col-sm-12
    [:section
     [:ul.arglists
      (if forms
        (map #($argform %) forms)
        (map #($arglist name %) arglists))]]]])

(defn var-page-handler [ns name]
  (let [name (util/cd-decode (codec/url-decode name))
        {:keys [arglists name ns doc runtimes added file] :as v} (lookup-var ns name)]
    (fn [{:keys [user session uri]}]
      (when v
        (let [examples (data/find-examples-for v)
              see-alsos (->> v
                             see-alsos-for
                             (map #(assoc % :can-delete? (util/is-author? user %))))
              library (library-for v)
              recent (:recent session)
              notes (->> v
                         data/find-notes-for
                         (map #(let [author? (util/is-author? user %)]
                                 (-> %
                                     (assoc :can-delete? author?)
                                     (assoc :can-edit? author?)))))
              canonical-url (config/url "/" ns "/" (util/cd-encode name))
              og-title (str name " - " ns)
              og-description (or (truncate-docstring doc 160)
                                 (str "Clojure var " ns "/" name))
              json-ld (json/generate-string
                        {"@context" "https://schema.org"
                         "@type" "SoftwareSourceCode"
                         "name" (str ns "/" name)
                         "description" (or doc (str "Clojure var " ns "/" name))
                         "url" canonical-url
                         "programmingLanguage" "Clojure"
                         "codeRepository" "https://github.com/clojure/clojure"})]
          {:session (update-in session [:recent]
                      #(->> %
                            (concat [{:text name
                                      :href (str "/" ns "/" (util/cd-encode name))}])
                            distinct
                            (filter :text)
                            (take 4)))
           :body
           (common/$main
             {:body-class (if (search/impl-var? v)
                            "var-page implementation-var"
                            "var-page")
              :title (util/html-encode (str name " - " ns " | ClojureDocs - Community-Powered Clojure Documentation and Examples"))
              :meta {"description" og-description
                     "twitter:card" "summary"
                     "twitter:title" og-title
                     "twitter:description" og-description}
              :og-meta {"og:title" og-title
                        "og:description" og-description
                        "og:url" canonical-url
                        "og:type" "website"
                        "og:site_name" "ClojureDocs"}
              :canonical-url canonical-url
              :json-ld json-ld
              :page-data {:examples (mapv clean-example examples)
                          :var v
                          :notes (vec (map clean-id notes))
                          :see-alsos (vec (map clean-see-also see-alsos))
                          :user (when user (select-keys user [:login :avatar-url :account-source]))}
              :page-uri uri
              :user user
              :mobile-nav [{:title "Nav"
                            :links [[:a {:href "#"
                                         :data-animate-scroll "true"
                                         :data-animate-buffer "70"}
                                     "Top"]
                                    [:a {:href "#examples"
                                         :data-animate-scroll "true"
                                         :data-animate-buffer "70"}
                                     "Examples "
                                     [:span.examples-count
                                      ($number-badge (count examples))]]
                                    [:a {:href "#see-also"
                                         :data-animate-scroll "true"
                                         :data-animate-buffer "70"}
                                     "See Also " ($number-badge (count see-alsos))]
                                    (when (> (count notes) 0)
                                      [:a {:href "#notes"
                                           :data-animate-scroll "true"
                                           :data-animate-buffer "70"}
                                       "Notes " ($number-badge (count notes))])]}
                           {:title "Namespaces"
                            :links (->> library
                                        :namespaces
                                        (map (fn [{:keys [name]}]
                                               [:a {:href (str "/" name)} name])))}]
              :content [:div
                        [:div.row
                         [:div.col-sm-2.sidenav
                          [:div.desktop-side-nav {:data-sticky-offset "10"}
                           [:div.var-page-nav]
                           (common/$library-nav library ns)]]
                         [:div.col-sm-10
                          ($var-header v)
                          ($implementation-notice v)
                          [:section
                           [:div.docstring
                            (if doc
                              [:pre (-> doc
                                        (str/replace #"\n\s\s" "\n")
                                        util/html-encode)]
                              [:div.null-state "No Doc"])
                            (when doc
                              [:div.copyright
                               "&copy; Rich Hickey. All rights reserved."
                               " "
                               [:a {:href "http://www.eclipse.org/legal/epl-v10.html"}
                                "Eclipse Public License 1.0"]])]]
                          [:section
                           [:div.examples-widget {:id "examples"}]]
                          [:section
                           [:div.see-alsos-widget {:id "see-also"}]]
                          [:section
                           [:div.notes-widget {:id "notes"}]]]]]})})))))

(defn $example-body [{:keys [body]}]
  [:div.example-body
   [:pre.raw-example {:class "brush: clojure"} body]])

(defn $example-history-point [{:keys [user body created-at updated-at] :as ex}]
  [:div.var-example
   [:div
    (let [num-to-show 7]
      [:div.example-meta
       [:div.contributors
        "Created by &nbsp;"
        (util/$avatar user)
        ""
        (util/timeago created-at) " ago."]
       [:div.links
        ]])]
   [:div ($example-body ex)]])

(defn example-handler [id]
  (fn [{:keys [user session uri]}]
    (let [{:keys [history name ns] :as ex}
          (mon/fetch-one :examples :where {:_id (util/bson-id id)})]
      (common/$main
         {:body-class "example-page"
          :user user
          :page-uri uri
          :content [:div.row
                    [:div.col-md-12
                     [:p
                      "Example history for "
                      (util/$var-link ns name (str ns "/" name))
                      ", in order from newest to oldest. "
                      "The currrent version is outlined in yellow."]
                     #_[:div.current-example
                      ($example ex)]
                     (->> history
                          reverse
                          (map $example-history-point))]]}))))
