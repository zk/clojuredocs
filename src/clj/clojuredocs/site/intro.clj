(ns clojuredocs.site.intro
  (:require [compojure.core :refer (defroutes GET)]
            [somnium.congomongo :as mon]
            [fogus.unk :refer (memo-ttl)]
            [clojuredocs.search :as search]
            [clojuredocs.site.common :as common]))

(defn $index [top-contribs]
  [:div
   [:div.row
    [:div.col-md-12
     [:section
      [:h1 "ClojureDocs is a community-powered documentation and examples repository for the " [:a {:href "http://clojure.org"} "Clojure programming language"] "."]]
     [:section
      [:form.search {:method :get :action "/search" :autocomplete "off"}
       [:input.form-control {:type "text"
                             :name "query"
                             :placeholder "Looking for?"
                             :autofocus "autofocus"
                             :autocomplete "off"}]]
      [:table.ac-results]]]]
   [:div.row
    [:div.col-md-6
     [:section
      [:h3 "Getting started with ClojureDocs"]
      [:p "Finding the right tool for the job can be tough, so we've outlined a few ways to go about your search below."]
      [:ul
       [:li [:i.icon-search] "Use the search box above to find what you're looking for."]
       [:li [:i.icon-map-marker] "Take a look at the Clojure Core quickref, which displays Clojure vars grouped by category."]
       [:li [:i.icon-book] "Browse an alphabetical list of vars defined in Clojure Core or Contrib."]]]]
    [:div.col-md-6
     [:section
      [:h3 "Top Contributors"]
      [:div.top-contribs
       (map common/$avatar top-contribs)]]]]
   [:div.row
    [:div.col-md-12
     [:h3 "Clojure is concise, powerful, and performant."]]
    [:div.col-md-6
     [:p
      "New to Clojure and not sure where to start? If you'd like to get a good background on Clojure's design origins (and be entertained at the same time), start "
      [:a {:href "http://www.infoq.com/presentations/Are-We-There-Yet-Rich-Hickey"} "here"]
      "."]
     [:p "If you're ready to jump in, then "
      [:a {:href ""} "here you go"]
      "."]]
    [:div.col-md-6
     [:div.example-code
      [:pre
       {:class "brush: clj"}
       (slurp "src/examples/clj/first.clj")]]]]
   [:div.row
    [:div.col-md-12
     [:h3 "On Clojure"]]
    [:div.col-md-6
     [:p "There's no denying that Clojure is just so "
      " *different* "
      " from what most of us are used to. "
      [:em "What is up with all those parentheses?!"]]
     [:p ]
     [:p "So it's no surprise that it"
      " takes a bit to get your head around. Stick with it, and you won't be disappointed."]]
    [:div.col-md-6
     [:p "But don't take our word for it, here's what XKCD has to say:"]
     [:p [:img {:src "https://imgs.xkcd.com/comics/lisp_cycles.png"}]]
     [:p "Seems like more than a few, these days. Happy coding!"]]
    [:div.col-md-12.used-by
     [:h3 "Clojure in Production"]
     [:ul
      (for [{:keys [src url]}
            [{:src "https://g.twimg.com/Twitter_logo_blue.png"
              :url "https://twitter.com"}
             {:src "https://upload.wikimedia.org/wikipedia/en/8/8b/Akamai_logo.svg"
              :url "http://www.akamai.com/"}
             {:src "https://upload.wikimedia.org/wikipedia/en/2/22/The_Climate_Corporation_Logo2.jpg"
              :url "http://www.climate.com/"}
             {:src "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Netflix_logo.svg/200px-Netflix_logo.svg.png"
              :url "https://www.netflix.com"}
             {:src "https://www.factual.com/assets/factual_logo_small-9d5ae614ae5422b251648ca62d6b4e51.png"
              :url "http://www.factual.com"}
             {:src "https://www.simple.com/img/logo-a2236763875.png"
              :url "https://simple.com"}
             {:src "https://upload.wikimedia.org/wikipedia/commons/c/cc/Groupon_logo.png"
              :url "http://www.groupon.com/"}
             {:src "https://d1lpkba4w1baqt.cloudfront.net/heroku-logo-light-234x60.png"
              :url "https://www.heroku.com"}
             {:src "https://img.brightcove.com/logo-corporate-new.png"
              :url "http://www.brightcove.com"}
             {:src "https://upload.wikimedia.org/wikipedia/en/9/92/SoundCloud_logo.svg"
              :url "https://soundcloud.com"}]]
        [:li [:a {:href url} [:img {:src src}]]])]]]
   [:div.row
    [:div.col-md-6
     [:section
      [:h3 "Contribute to ClojureDocs"]
      [:p "We need your help to make ClojureDocs a great community resource. Here are a couple of ways you can contribute."]
      [:ul
       [:li
        [:h4 [:i.icon-comment-alt] "Give Feedback"]
        [:p "Please " [:a {:href "https://github.com/zk/clojuredocs/issues"} "open a ticket"] " if you have an idea of how we can improve ClojureDocs."]]
       [:li
        [:h4 [:i.icon-indent-right] "Add an Example"]
        [:p "Sharing your knowledge with fellow Clojurists is easy:"]
        [:p "First, take a look at the examples style guide, and then add an example for your favorite var (or pick one from the list)."]
        [:p "In addition to examples, you also have the ability to add 'see also' references between vars."]]]]]]])

(defn top-contribs []
  (let [scores (atom {})]
    (doseq [{:keys [history]} (mon/fetch :examples)]
      (let [history (reverse history)
            first-user (-> history first :user)]
        (swap! scores update-in [first-user] #(+ 4 (or % 0)))
        (doseq [user (->> history rest (map :user))]
          (swap! scores update-in [user] #(inc (or % 0))))))
    (->> @scores
         (sort-by second)
         reverse
         (take (* 9 5))
         (map #(assoc (first %) :score (second %))))))

(def top-contribs (memo-ttl top-contribs (* 1000 60 60 6)))

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
       :body (pr-str (search/query (:query params)))})))



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
