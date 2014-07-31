(ns clojuredocs.site.intro
  (:require [clojuredocs.config :as config]
            [compojure.core :refer (defroutes GET POST)]
            [somnium.congomongo :as mon]
            [fogus.unk :refer (memo-ttl)]
            [clojuredocs.search :as search]
            [clojuredocs.util :as util]
            [clojuredocs.site.common :as common]
            [clojure.edn :as edn]))

(defn $index [top-contribs]
  [:div
   [:div.row
    [:div.col-md-12
     [:section
      [:h1 "ClojureDocs is a community-powered documentation and examples repository for the " [:a {:href "http://clojure.org"} "Clojure programming language"] "."]]
     [:section
      [:div.search-widget
       [:form.search {:method :get :action "/search" :autocomplete "off"}
        [:input.form-control.placeholder
         {:type "text"
          :name "query"
          :placeholder "Looking for?"
          :autoFocus "autofocus"
          :autoComplete "off"}]
        [:ul.ac-results]
        [:div.not-finding
         "Can't find what you're looking for? " [:a {:href "/search-feedback"} "Help make ClojureDocs better"] "."]]]]]]
   [:div.row
    [:div.col-md-12
     [:section
      [:h5 "Top Contributors"]
      [:div.top-contribs
       (if-not (empty? top-contribs)
         (map common/$avatar top-contribs)
         [:div.null-state "Uh-oh, no contributors!"])]]]]
   [:section
    [:div.row
     [:div.col-md-12
      [:h5 "On Clojure"]]
     [:div.col-md-6
      [:p "Clojure is a concise, powerful, and performant general-purpose programming language that favors simplicity, composibility, functional and a data-oriented way of
solving problems (holy buzzwords, fix this)."]
      [:p
       "New to Clojure and not sure where to start? If you'd like to get a good background on Clojure's design origins (and be entertained at the same time), start "
       [:a {:href "http://www.infoq.com/presentations/Are-We-There-Yet-Rich-Hickey"} "here"]
       "."]
      [:p "If you're ready to jump in, then start with the following resources:"]
      [:ul.getting-started-resources
       [:li [:a {:href "http://tryclj.com"} "Try Clojure (in your browser)"]]
       [:li [:a {:href "http://www.braveclojure.com"} "Clojure for the Brave and True"]]
       [:li [:a {:href "http://clojurescriptkoans.com/"} "ClojureScript Koans"]]
       [:li [:a {:href "http://4clojure.org"} "4Clojure (learn Clojure interactively)"]]]
      [:p "There's no denying that Clojure is just so "
       " *different* "
       " from what most of us are used to (what is up with all those parentheses?!). "
       "So it's no surprise that it"
       " takes a bit to get your head around. Stick with it, and you won't be disappointed."]
      [:p "But don't take our word for it, here's what XKCD has to say:"]
      [:p [:img.xkcd {:src "/img/lisp_cycles.png"}]]
      [:p "Seems like more than a few, these days. Happy coding!"]]
     [:div.col-md-6
      [:div.example-code
       [:pre
        {:class "brush: clj"}
        (slurp "src/examples/clj/first.clj")]]]]]
   [:div.row
    [:div.col-md-12
     [:section.used-by
      [:h5 "Clojure in Production"]
      [:ul
       (for [{:keys [src url]}
             [{:src "https://g.twimg.com/Twitter_logo_blue.png"
               :url "https://twitter.com"}
              {:src "https://upload.wikimedia.org/wikipedia/en/2/22/The_Climate_Corporation_Logo2.jpg"
               :url "http://www.climate.com/"}
              {:src "/img/netflix-logo.png"
               :url "https://www.netflix.com"}
              {:src "/img/factual-logo.png"
               :url "http://www.factual.com"}
              {:src "/img/prismatic-logo.png"
               :url "https://getprismatic.com"}
              {:src "https://www.simple.com/img/logo-a_2x.CREAM-bbc497f18505b852.png"
               :url "https://simple.com"}
              {:src "https://d1lpkba4w1baqt.cloudfront.net/heroku-logo-light-234x60.png"
               :url "https://www.heroku.com"}
              {:src "https://img.brightcove.com/logo-corporate-new.png"
               :url "http://www.brightcove.com"}
              {:src "https://upload.wikimedia.org/wikipedia/en/9/92/SoundCloud_logo.svg"
               :url "https://soundcloud.com"}
              {:src "https://puppetlabs.com/wp-content/uploads/2010/12/PL_logo_horizontal_RGB_sm.png"
               :url "https://puppetlabs.com"}
              {:src "/img/living-social-logo.png"
               :url "https://livingsocial.com"}]]
         [:li [:a {:href url} [:img {:src src}]]])]]]]
   [:div.row
    [:div.col-md-6
     [:section
      [:h5 "Contribute to ClojureDocs"]
      [:p "We need your help to make ClojureDocs a great community resource. Here are a couple of ways you can contribute."]
      [:ul
       [:li
        [:h4 [:i.fa.fa-comment-o] "Give Feedback"]
        [:p "Please " [:a {:href "https://github.com/zk/clojuredocs/issues"} "open a ticket"] " if you have an idea of how we can improve ClojureDocs."]]
       [:li
        [:h4 [:i.fa.fa-indent] "Add an Example"]
        [:p "Sharing your knowledge with fellow Clojurists is easy:"]
        [:p "First, take a look at the examples style guide, and then add an example for your favorite var (or pick one from the list)."]
        [:p "In addition to examples, you also have the ability to add 'see also' references between vars."]]]]]]])

(defn top-contribs []
  (let [scores (atom {})]
    (doseq [{:keys [history user]} (mon/fetch :examples)]
      (let [history (->> history
                         (concat [{:user user}])
                         reverse)
            first-user (-> history first :user)]
        (swap! scores update-in [first-user] #(+ 4 (or % 0)))
        (doseq [user (->> history rest (map :user))]
          (swap! scores update-in [user] #(inc (or % 0))))))
    (->> @scores
         (sort-by second)
         reverse
         (take (* 24 3))
         (map #(assoc (first %) :score (second %))))))

;; :|
(when-not config/cljs-dev?
  (def top-contribs (memo-ttl top-contribs (* 1000 60 60 6))))

(defn add-see-alsos [results]
  (let [sa-lookup (->> results
                       (map #(select-keys % [:ns :name]))
                       (map (fn [l]
                              [l (->> (mon/fetch-one :see-alsos :where l)
                                      :vars
                                      (map (fn [{:keys [ns name]}]
                                             {:ns ns
                                              :name name
                                              :href (str "/" ns "/" name)})))]))
                       (into {}))]
    (->> results
         (map #(assoc % :see-alsos (get sa-lookup (select-keys % [:ns :name])))))))

(defn add-examples-count [results]
  (let [examples-lookup (->> results
                             (map #(select-keys % [:ns :name]))
                             (map (fn [l]
                                    [l (mon/fetch-count :examples :where l)]))
                             (into {}))]
    (->> results
         (map #(assoc % :examples-count (get examples-lookup (select-keys % [:ns :name])))))))


(defn search-feedback [{:keys [params uri user]}]
  (common/$main
    {:body-class "search-feedback-page"
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-12
       [:h3 "Send us a note on how we can improve ClojureDocs."]
       [:p "We're sorry you couldn't find what you were looking for. If you leave us a note below with what you were looking for and how you tried to find it, we can use your feedback to make the site better."]
       [:div.search-feedback-widget
        {:data-query (:query params)}]]]}))

(defn core-markdown []
  (-> "src/md/core-library.md"
      slurp
      util/markdown))

(when config/cache-markdown?
  (def core-markdown (memoize core-markdown)))


(defn core-library [{:keys [user uri]}]
  (common/$main
    {:body-class "core-library-page"
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-12
       (core-markdown)]]}))

(defroutes routes
  (GET "/" []
    (fn [{:keys [user]}]
      (-> {:content ($index (top-contribs))
           :body-class "intro-page"
           :hide-search true
           :user user}
          common/$main)))

  (GET "/search" []
    (fn [{:keys [params]}]
      {:headers {"Content-Type" "application/edn"}
       :body (pr-str (->> params
                          :query
                          search/query
                          add-see-alsos
                          add-examples-count))}))

  (GET "/search-feedback" [] search-feedback)
  (POST "/search-feedback" [] (fn [{:keys [edn-body]}]
                                (try
                                  (mon/insert! :search-feedback
                                    (assoc edn-body :created-at (util/now)))
                                  {:status 200
                                   :body "Ok!"}
                                  (catch Exception e
                                    (.printStackTrace e)
                                    {:body (str "Whoops, something went wrong. " (.getMessage e))
                                     :status 500}))))
  (GET "/search-feedback/success"
    [] (fn [{:keys [user uri]}]
         (common/$main
           {:body-class "search-feedback-page"
            :user user
            :page-uri uri
            :content
            [:div.row
             [:div.col-md-12
              [:h3 "Thanks for your feedback"]
              [:p "Perhaps you can find what you're looking for using our "
               [:a {:href "/quickref"} "Clojure quick reference"]
               "."]]]})))

  (GET "/core-library" [] core-library))



;; Scratch for front page example code
(comment

;; Function definition

(defn get-subject [scene]
  (get scene :subject))


;; Functions are first-class in Clojure

(map get-subject theater)
;;=> ("Frankie", "Lucy")


;; And can be anonymous

(map (fn [scene] (get scene :object)) theater)
;;=> ("relax" "Clojure")


;; And short-handed (hah!)

(map #(get % :action) theater)
;;=> ("says" "❤s")


;; Though idiomatically, it's:

(map :action theater)
;;=> ("says" "❤s")


;; baby shoes, never used.


;; Let's define a variable

(def story ["used" "never" "shoes" "baby" "sale" "for"])

;; Clojure has literals for keywords,
;; lists, maps, and much more.

(println story) ;;=> [:used :never :shoes :baby :sale :for]

;; Not much of a story, yet. We need to
;; reorder, and insert some punctuation

(->> story
     (reverse)                          ; reverse the list
     (partition 2)                      ; break it up into chunks of 2
     (map #(interpose " " %))
     (interpose ", ")                   ; insert spaces
     flatten
     (apply str))
)
