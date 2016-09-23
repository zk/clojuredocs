(ns clojuredocs.pages.jobs
  (:require [clojuredocs.util :as util]
            [somnium.congomongo :as mon]
            [clojuredocs.pages.common :as common]
            [clojure.string :as str]))

(defn format-currency [s]
  (condp = s
    "USD" "$"
    "EUR" "€"
    "GBP" "£"
    s))

(defn delim-for [s]
  (condp = s
    "USD" ","
    "EUR" "."
    "GBP" "."
    ","))

(defn format-number [s & [delim]]
  (->> s
       str
       reverse
       (partition-all 3)
       (interpose (or delim ","))
       flatten
       reverse
       (apply str)))

(defn url-safe [s]
  (-> s
      (str/replace " " "-")
      (str/replace #"[^a-zA-Z0-9-_]" "")))

(defn job-slug [{:keys [job-title company-name]}]
  (->> (str company-name "-" job-title)
       url-safe
       str/lower-case))

(def DATA [{:job-title "Site Reliability Engineer"
            :job-type "Permanent"
            :job-location "Manhatten, New York"
            :short-id "xD"
            :posted-at (util/now)
            :visa-sponsorship? true
            :comp {:currency "EUR"
                   :minimum 50000
                   :maximum 100000}
            :job-description "Atlassian helps teams everywhere change the world through the power of software and we are looking for a technical wizard to help Synchrony's development team. Synchrony is the engine behind Confluence's co-editing feature.

As a Senior Developer, you’ll be crafting and implementing with the team a data synchronisation service for tree data structures using operational transformation on a large scale. All the hard problem of OT with nested tree data, n-squared complexity issue, support of key-value maps and many other challenges are solved in Synchrony.

We are supported by robust deployment systems, mature algorithmic solutions, and an experienced team with a strong desire to build the best synchronisation technology out there. Think Nasa launching the Rover on Mars or Cochlear gifting children born deaf with the ability to hear, your work directly impacts the products they use to advance humanity. You'll need a strong technical prowess with incredible coding ability across a diverse set of languages and technologies."
            :key-technologies ["Clojure" "ClojureScript" "Ruby-on-Rails"]
            :skills "* One\n* Two\n* Three"
            :company-name "Google, Inc"
            :company-url "https://google.com"
            :company-image-url "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/1024px-Google_%22G%22_Logo.svg.png"
            :company-description "With critically-acclaimed and award-winning hits that include BILLIONS, THE AFFAIR, HOMELAND, RAY DONOVAN, MASTERS OF SEX, SHAMELESS, DEXTER®, HOUSE OF LIES, EPISODES and PENNY DREADFUL, Showtime Networks Inc. (SNI) has firmly established itself as a producer and provider of quality original programming. Original series play a key part in the SHOWTIME programming mix, along with box office hits, comedy and music specials, provocative documentaries, and hard-hitting sports programming, including the flagship franchise SHOWTIME CHAMPIONSHIP BOXING®, the Emmy Award-winning veteran series INSIDE THE NFL™ and 60 MINUTES SPORTS™. SNI also owns and manages Smithsonian Channel™, through its joint venture with the Smithsonian Institution. Showtime Networks Inc. (SNI), a wholly-owned subsidiary of CBS Corporation, owns and operates the premium television networks SHOWTIME®, THE MOVIE CHANNEL™ and FLIX®, and also offers SHOWTIME ON DEMAND®, THE MOVIE CHANNEL™ ON DEMAND and FLIX ON DEMAND®, and the network's authentication service SHOWTIME ANYTIME®. Showtime Digital Inc., a wholly-owned subsidiary of SNI, operates the stand-alone streaming service SHOWTIME®. SHOWTIME is currently available to subscribers via cable, DBS and telco providers, and as a stand-alone streaming service through Apple®, Roku®, Amazon and Google. Consumers can also subscribe to SHOWTIME via Hulu, Sony PlayStation® Vue and Amazon Prime Video. SNI also manages Smithsonian Networks™, a joint venture between SNI and the Smithsonian Institution, which offers Smithsonian Channel™, and offers Smithsonian Earth™ through SN Digital LLC. SNI markets and distributes sports and entertainment events for exhibition to subscribers on a pay-per-view basis through SHOWTIME PPV®."
            :company-address "Times Square, New York, NY"}])

(defn $job-preview [{:keys [job-title job-type
                            job-location
                            job-description
                            short-id
                            posted-at
                            comp
                            company-name company-image-url]
                     :as job}]
  [:div.row
   {:style "border-bottom:solid #ccc 1px;padding-bottom:20px;position:relative"}
   [:div.col-sm-2.text-center
    [:img {:src company-image-url
           :style "width:100%;max-width:150px"}]]
   [:div.col-sm-10
    {:style "vertical-align:top;"}
    [:div
     [:h3 {:style "margin:0"}
      [:a {:href (str "/jobs/" short-id "/" (job-slug job))} job-title]]
     (when comp
       [:div.compensation {:style "position:absolute;top:5px;right:15px;color:#aaa"}
        (format-currency
          (:currency comp))
        (format-number
          (:minimum comp)
          (delim-for (:currency comp)))
        "-"
        (format-number (:maximum comp)
          (delim-for (:currency comp)))])]
    [:h4 {:style "font-size:14px;margin:0"}
     [:span.company-name
      {:style "margin-right:10px;font-weight:normal"}
      company-name]
     [:span.job-location
      {:style "color:#888;"}
      [:i.fa.fa-map-marker
       {:style "margin-right:5px"}]
      job-location]]
    [:div.description-preview
     {:style "margin-top:0px;font-size:13px;color:#777;font-weight:300"}
     (util/ellipsis 80 job-description)]
    [:div.posted-at.text-right
     {:style "font-size:10px;font-weight:bold;text-transform:uppercase;color:#aaa;margin-top:3px"}
     "Posted " (util/timeago posted-at) " ago"]
    ]])

(defn list-handler [{:keys [params uri user]}]
  (common/$main
    {:body-class "jobs-page"
     :title (str "Send Us Some Feedback | ClojureDocs - Community-Powered Clojure Documentation and Examples")
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-12
       [:h2 {:style "text-align:center;border-bottom:solid #ccc 1px;padding-bottom:10px"} "Latest Jobs"]
       (->> DATA
            (map $job-preview))]]}))

(defn single-handler [job-id]
  (fn [{:keys [params uri user]}]
    (common/$main
      {:body-class "jobs-page"
       :title (str "Send Us Some Feedback | ClojureDocs - Community-Powered Clojure Documentation and Examples")
       :user user
       :page-uri uri
       :content
       [:div.row
        [:div.col-md-12
         [:h3 "Jobs"]]]})))
