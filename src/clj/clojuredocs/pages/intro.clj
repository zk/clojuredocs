(ns clojuredocs.pages.intro
  (:require [clojuredocs.util :as util]
            [somnium.congomongo :as mon]
            [clojuredocs.pages.common :as common]
            [clojuredocs.syntax :as syntax]))

(defmulti $render-recently-updated :type)

(defmethod $render-recently-updated :default [_] nil)

(defmethod $render-recently-updated :example
  [{:keys [var author created-at]}]
  [:div.recently-updated
   (util/$avatar author {:size 48})
   [:span.content
    (:login author)
    " authored an example for "
    (util/$var-link (:ns var) (:name var)
      (-> var :ns util/html-encode)
      "/"
      (-> var :name util/html-encode))
    " "
    (util/timeago created-at)
    " ago."
    [:div.clear]]])

(defmethod $render-recently-updated :see-also
  [{:keys [from-var to-var author created-at]}]
  [:div.recently-updated
   (util/$avatar author {:size 48})
   [:span.content
    (:login author)
    " added a see-also from "
    (util/$var-link (:ns from-var) (:name from-var)
      (-> from-var :ns util/html-encode)
      "/"
      (-> from-var :name util/html-encode))
    " to "
    (util/$var-link (:ns to-var) (:name to-var)
      (-> to-var :ns util/html-encode)
      "/"
      (-> to-var :name util/html-encode))
    " "
    (util/timeago created-at)
    " ago."
    [:div.clear]]])

(defmethod $render-recently-updated :note
  [{:keys [var author created-at]}]
  [:div.recently-updated
   (util/$avatar author {:size 48})
   [:span.content
    (:login author)
    " authored a note for "
    (util/$var-link (:ns var) (:name var)
      (-> var :ns util/html-encode)
      "/"
      (-> var :name util/html-encode))
    " "
    (util/timeago created-at)
    " ago."]
   [:div.clear]])

(defn $index [recently-updateds]
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
          :name "q"
          :placeholder "Looking for?"
          :autoFocus "autofocus"
          :autoComplete "off"}]
        [:ul.ac-results]]]]]]
   [:div.row
    [:div.col-md-12
     [:section
      [:h5 "Recently Updated"]
      [:div.row
       (->> recently-updateds
            (map $render-recently-updated)
            (partition-all 3)
            (map (fn [rs]
                   [:div.col-sm-6 rs])))]]]]
   [:div.row
    [:div.col-md-12
     [:section.used-by
      [:h5 "Clojure in Production"]
      [:ul
       (for [{:keys [src url]}
             [{:src "/img/nubank-logo.png"
               :url "https://nubank.com.br/en/"}
              {:src "/img/netflix-logo.png"
               :url "https://www.netflix.com"}
              {:src "/img/amazon-logo.png"
               :url "http://www.amazon.com"}
              {:src "/img/climate-corporation-logo.png"
               :url "http://www.climate.com/"}
              {:src "/img/funding-circle-logo.png"
               :url "https://www.fundingcircle.com"}
              {:src "https://help.twitter.com/content/dam/help-twitter/brand/logo.png"
               :url "https://twitter.com"}
              {:src "/img/factual-logo.png"
               :url "http://www.factual.com"}
              {:src "/img/exoscale-logo.png"
               :url "https://www.exoscale.com/"}
              {:src "/img/heroku-logo.png"
               :url "https://www.heroku.com"}
              {:src "/img/brightcove-logo.png"
               :url "http://www.brightcove.com"}
              {:src "/img/soundcloud-logo.png"
               :url "https://soundcloud.com"}
              {:src "/img/puppet-logo.jpg"
               :url "https://puppet.com"}
              {:src "/img/living-social-logo.png"
               :url "https://livingsocial.com"}
              {:src "/img/circleci-logo.png"
               :url "https://circleci.com"}
              {:src "/img/walmart-labs.png"
               :url "http://www.walmartlabs.com"}
              {:src "/img/oscaro-logo.png"
               :url "https://www.oscaro.com/"}
              {:src "/img/rjmetrics-logo.png"
               :url "https://rjmetrics.com"}
              {:src "/img/cognician-logo.png"
               :url "https://www.cognician.com"}
              {:src "/img/qubit-logo.png"
               :url "https://qubit.com/"}
              {:src "/img/kira-logo.png"
               :url "https://kirasystems.com"}
              {:src "/img/farmlogs-logo.png"
               :url "http://www.farmlogs.com"}
              {:src "/img/adzerk-logo.png"
               :url "http://adzerk.com"}
              {:src "/img/witai-logo.png"
               :url "http://wit.ai"}
              {:src "/img/clearwater-logo.png"
               :url "https://clearwateranalytics.com/"}
              {:src "/img/griffin-logo.png"
               :url "https://griffin.sh/"}
              {:src "/img/whimsical-logo.png"
               :url "https://whimsical.com/"}
              {:src "/img/pitch-logo.png"
               :url "https://pitch.com/"}
              {:src "/img/fluent-logo.png"
               :url "https://fluent.to/"}
              {:src "/img/reify-health-logo.png"
               :url "https://www.reifyhealth.com/"}
              {:src "/img/pisano-logo.jpg"
               :url "https://www.pisano.co"}]]
         [:li [:a {:href url} [:img {:src src}]]])]]]]
   [:div.row
    [:div.col-md-6
     [:section
      [:h5 "Contribute to ClojureDocs"]
      [:p "We need your help to make ClojureDocs a great community resource. Here are a couple of ways you can contribute."]
      [:ul
       [:li
        [:h4 [:i.fa.fa-comment-o] "Give Feedback"]
        [:p "Please " [:a {:href "https://github.com/nubank/clojuredocs/issues"} "open a ticket"] " if you have an idea of how we can improve ClojureDocs."]]
       [:li
        [:h4 [:i.fa.fa-indent] "Add an Example"]
        [:p "Sharing your knowledge with fellow Clojurists is easy:"]
        [:p
         "First, take a look at the "
         [:a {:href "/examples-styleguide"} "examples style guide"]
         ", and then add an example for your favorite var (or pick one from the list)."]
        [:p "In addition to examples, you also have the ability to add 'see also' references between vars."]]]]]]])


(defn recently-updated []
  (let [limit 6
        examples (->> (mon/fetch :examples
                        :where {:deleted-at nil}
                        :sort {:created-at -1}
                        :limit limit)
                      (map #(assoc % :type :example)))
        see-alsos (->> (mon/fetch :see-alsos :sort {:created-at -1} :limit limit)
                       (map #(assoc % :type :see-also)))
        notes (->> (mon/fetch :notes :sort {:created-at -1} :limit limit)
                   (map #(assoc % :type :note)))]
    (->> (concat
           examples
           see-alsos
           notes)
         (sort-by :created-at)
         reverse
         (take limit))))

(defn page-handler [{:keys [user]}]
    (-> {:content ($index (recently-updated))
      :body-class "intro-page"
      :user user
      :hide-search true
      :show-stars? true}
     common/$main))
