(ns clojuredocs.pages.intro
  (:require [clojuredocs.config :as config]
            [clojuredocs.util :as util]
            [compojure.core :refer (defroutes GET POST)]
            [somnium.congomongo :as mon]
            [fogus.unk :refer (memo-ttl)]
            [clojuredocs.pages.common :as common]
            [clojuredocs.syntax :as syntax]))

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
         (map util/$avatar top-contribs)
         [:div.null-state "Uh-oh, no contributors!"])
       [:div.migrate-account
        [:a {:href "/migrate-account"} "Migrate your old ClojureDocs account"]]]]]]
   [:section
    [:div.row
     [:div.col-md-12
      [:h5 "On Clojure"]]
     [:div.col-md-6
      [:p "Clojure is a concise, powerful, and performant general-purpose programming language that runs on the JVM, CLR, Node.js, and modern mobile and desktop web browsers."]
      [:p
       "New to Clojure and not sure where to start? Here are a few good resources to get you off on the right foot:"]
      [:ul.getting-started-resources
       [:li [:a {:href "http://www.infoq.com/presentations/Are-We-There-Yet-Rich-Hickey"} "Are We There Yet? (video)"]]
       [:li [:a {:href "http://www.braveclojure.com"} "Clojure for the Brave and True"]]
       [:li [:a {:href "http://tryclj.com"} "Try Clojure (in your browser)"]]
       [:li [:a {:href "http://clojurescriptkoans.com/"} "ClojureScript Koans"]]
       [:li [:a {:href "http://4clojure.org"} "4Clojure (learn Clojure interactively)"]]]
      [:p "There's no denying that Clojure is just so "
       " *different* "
       " from what most of us are used to (what is up with all those parentheses?!). "
       "So it's no surprise that it"
       " takes a bit to get your head around it. Stick with it, and you won't be disappointed."]
      [:p "But don't take our word for it, here's what XKCD has to say:"]
      [:p [:img.xkcd {:src "/img/lisp_cycles.png"}]]
      [:p "Seems like more than a few, these days. Happy coding!"]]
     [:div.col-md-6
      [:div.example-code
       (-> "src/examples/clj/first.clj"
           slurp
           (syntax/syntaxify :stringify-style? true))]]]]
   [:div.row
    [:div.col-md-12
     [:section.used-by
      [:h5 "Clojure in Production"]
      [:ul
       (for [{:keys [src url]}
             [{:src "/img/netflix-logo.png"
               :url "https://www.netflix.com"}
              {:src "https://upload.wikimedia.org/wikipedia/en/2/22/The_Climate_Corporation_Logo2.jpg"
               :url "http://www.climate.com/"}
              {:src "https://g.twimg.com/Twitter_logo_blue.png"
               :url "https://twitter.com"}
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
               :url "https://livingsocial.com"}
              {:src "/img/circleci-logo.png"
               :url "https://circleci.com"}
              {:src "/img/walmart-labs.png"
               :url "http://www.walmartlabs.com"}
              {:src "/img/rjmetrics-logo.png"
               :url "https://rjmetrics.com"}
              {:src "/img/cognician-logo.png"
               :url "https://www.cognician.com"}
              {:src "/img/qubit-logo.png"
               :url "https://www.qubitproducts.com/"}
              {:src "/img/diligenceengine-logo.png"
               :url "https://diligenceengine.com/"}]]
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
        [:p
         "First, take a look at the "
         [:a {:href "/examples-style-guide"} "examples style guide"]
         ", and then add an example for your favorite var (or pick one from the list)."]
        [:p "In addition to examples, you also have the ability to add 'see also' references between vars."]]]]]]])


(defn top-contribs []
  (let [scores (atom {})]
    (doseq [{:keys [author _id]} (mon/fetch :examples :where {:deleted-at nil})]
      (let [editors (->> (mon/fetch :example-histories :where {:example-id _id})
                         (map :editor))]
        (swap! scores update-in [author] #(+ 4 (or % 0)))
        (doseq [editor editors]
          (swap! scores update-in [editor] #(inc (or % 0))))))
    (->> @scores
         (sort-by second)
         reverse
         (remove #(get #{"zkim" "zk"} (-> % first :login)))
         (take (* 24 3))
         (map #(assoc (first %) :score (second %))))))

;; :|
(when-not config/cljs-dev?
  (def top-contribs (memo-ttl top-contribs (* 1000 60 60 6))))

(defn page-handler [{:keys [user]}]
  (-> {:content ($index (top-contribs))
       :body-class "intro-page"
       :hide-search true
       :user user}
      common/$main))
