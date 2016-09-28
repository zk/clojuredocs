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
            :job-apply-href "https://www.google.com"
            :short-id "xD"
            :remote-ok? true
            :posted-at (util/now)
            :comp {:currency "EUR"
                   :minimum 50000
                   :maximum 100000}
            :job-description "Atlassian helps teams everywhere change the world through the power of software and we are looking for a technical wizard to help Synchrony's development team. Synchrony is the engine behind Confluence's co-editing feature.

## HELLO WORLD

As a Senior Developer, you’ll be crafting and implementing with the team a data synchronisation service for tree data structures using operational transformation on a large scale. All the hard problem of OT with nested tree data, n-squared complexity issue, support of key-value maps and many other challenges are solved in Synchrony.

### Stuff

We are supported by robust deployment systems, mature algorithmic solutions, and an experienced team with a strong desire to build the best synchronisation technology out there. Think Nasa launching the Rover on Mars or Cochlear gifting children born deaf with the ability to hear, your work directly impacts the products they use to advance humanity. You'll need a strong technical prowess with incredible coding ability across a diverse set of languages and technologies.

**Types of projects we do**

* Built a community-based  competition/contest platform that connects musicians with major artists and brands for the creation of original songs as well as remixes. indabamusic.com
* Built the world’s premiere royalty free sample library. converse.com/samplelibrary
* Building unprecedented platform for creating, distributing and monetizing derivative works (e.g. sampled songs, remixes) by bringing content, composers, musicians, and producers together."
            :key-technologies ["Clojure" "ClojureScript" "Ruby-on-Rails"]
            :skills "* One\n* Two\n* Three"
            :company-name "Google, Inc"
            :company-url "https://google.com"
            :company-image-url "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/1024px-Google_%22G%22_Logo.svg.png"
            :company-description "With critically-acclaimed and award-winning hits that include BILLIONS, THE AFFAIR, HOMELAND, RAY DONOVAN, MASTERS OF SEX, SHAMELESS, DEXTER®, HOUSE OF LIES, EPISODES and PENNY DREADFUL, Showtime Networks Inc. (SNI) has firmly established itself as a producer and provider of quality original programming. Original series play a key part in the SHOWTIME programming mix, along with box office hits, comedy and music specials, provocative documentaries, and hard-hitting sports programming, including the flagship franchise SHOWTIME CHAMPIONSHIP BOXING®, the Emmy Award-winning veteran series INSIDE THE NFL™ and 60 MINUTES SPORTS™. SNI also owns and manages Smithsonian Channel™, through its joint venture with the Smithsonian Institution. Showtime Networks Inc. (SNI), a wholly-owned subsidiary of CBS Corporation, owns and operates the premium television networks SHOWTIME®, THE MOVIE CHANNEL™ and FLIX®, and also offers SHOWTIME ON DEMAND®, THE MOVIE CHANNEL™ ON DEMAND and FLIX ON DEMAND®, and the network's authentication service SHOWTIME ANYTIME®. Showtime Digital Inc., a wholly-owned subsidiary of SNI, operates the stand-alone streaming service SHOWTIME®. SHOWTIME is currently available to subscribers via cable, DBS and telco providers, and as a stand-alone streaming service through Apple®, Roku®, Amazon and Google. Consumers can also subscribe to SHOWTIME via Hulu, Sony PlayStation® Vue and Amazon Prime Video. SNI also manages Smithsonian Networks™, a joint venture between SNI and the Smithsonian Institution, which offers Smithsonian Channel™, and offers Smithsonian Earth™ through SN Digital LLC. SNI markets and distributes sports and entertainment events for exhibition to subscribers on a pay-per-view basis through SHOWTIME PPV®."}

           {:job-title "Senior Software Engineer - Web Development"
            :job-location "San Francisco, CA"
            :job-apply-href "mailto:jobs@asdf.com"
            :short-id "asdf"
            :remote-ok? true
            :posted-at (util/now)
            :job-description "**Senior Software Engineer - Web Development**

HQ - San Francisco, CA

FiveStars is the rewards program for small businesses that’s proven to bring customers back more often. Through unique rewards and personalized service, FiveStars helps everybody be a VIP. Founded in 2011 and based in San Francisco, our mission is to help businesses and communities thrive by turning every transaction into a relationship. In 2015, FiveStars drove 35 million in-store visits across over 10,000 local businesses in the U.S. and Canada. Over 10 million consumers use FiveStars to have exceptional experiences with local businesses. To-date FiveStars has raised $105 million from top-tier investors including HarbourVest, Menlo Ventures, Lightspeed, DCM, and Y-Combinator.

Beyond our in-store experience we provide our merchants with a powerful dashboard to monitor their loyalty campaigns, make tweaks, and directly communicate with their customers. If you have strong CS fundamentals, an eye for detail, and want to build tools that thousands of small businesses use to engage their best customers, then we are looking for you! Our web development team has a wide range of roles and responsibilities including front end engineering in Angular JS, backend API work in Django, and asynchronous pipeline management using Celery and RabbitMQ.

**Responsibilities:**

* Drive product development on our rich web applications, enabling our merchants to make better decisions and stronger connections with their customers
* Design for scale - our data is growing exponentially and we need new solutions to keep up with that growth
* Work with the FiveStars Design team to build out our engineering processes and speed up the Product Vision -> Design -> Implementation cycle
* Maintain a high level of quality in our code bases through well-written automated tests and good architecture and code review practices
* Invest in FiveStars and its culture - our values are something we live and breathe every day
* Love to pair program and do not mind reviewing code (maybe even enjoy it)

As a Senior Developer, you’ll be crafting and implementing with the team a data synchronisation service for tree data structures using operational transformation on a large scale. All the hard problem of OT with nested tree data, n-squared complexity issue, support of key-value maps and many other challenges are solved in Synchrony.

We are supported by robust deployment systems, mature algorithmic solutions, and an experienced team with a strong desire to build the best synchronisation technology out there. Think Nasa launching the Rover on Mars or Cochlear gifting children born deaf with the ability to hear, your work directly impacts the products they use to advance humanity. You'll need a strong technical prowess with incredible coding ability across a diverse set of languages and technologies."
            :company-name "FiveStars"
            :company-url "http://fivestars.com"
            :company-image-url "http://i.stack.imgur.com/9hUaP.jpg"
            :company-description "FiveStars is the rewards program for small businesses that’s proven to bring customers back more often. Through unique rewards and personalized service, FiveStars helps everybody be a VIP. Founded in 2011 and based in San Francisco, our mission is to help businesses and communities thrive by turning every transaction into a relationship. In 2015, FiveStars drove 35 million in-store visits across over 10,000 local businesses in the U.S. and Canada. Over 10 million consumers use FiveStars to have exceptional experiences with local businesses. To-date FiveStars has raised $105 million from top-tier investors including HarbourVest, Menlo Ventures, Lightspeed, DCM, and Y-Combinator."}])

(defn currency-range [comp]
  (str
    (format-currency
      (:currency comp))
    (format-number
      (:minimum comp)
      (delim-for (:currency comp)))
    "-"
    (format-number (:maximum comp)
      (delim-for (:currency comp)))))

(defn $job-preview [{:keys [job-title job-type
                            job-location
                            job-description
                            short-id
                            posted-at
                            comp
                            company-name company-image-url]
                     :as job}
                    &
                    [{:keys [description?
                             comp?
                             posted-at?]}]]
  [:div.row
   {:style "position:relative"}
   [:div.col-sm-2.text-center
    [:img {:src company-image-url
           :style "width:100%;max-width:150px"}]]
   [:div.col-sm-10
    {:style "vertical-align:top;"}
    [:div
     [:h3 {:style "margin:0"}
      [:a {:href (str "/jobs/" short-id "/" (job-slug job))} job-title]]]
    [:h4 {:style "font-size:14px;margin:0"}
     [:span.company-name
      {:style "margin-right:10px;font-weight:normal"}
      company-name]
     [:span.job-location
      {:style "color:#888;"}
      [:i.fa.fa-map-marker
       {:style "margin-right:5px"}]
      job-location]]
    (when (and comp)
      [:div.compensation {:style "color:#aaa"}
       (currency-range comp)])
    (when description?
      [:div.description-preview
       {:style "margin-top:0px;font-size:13px;color:#777;font-weight:300"}
       (util/ellipsis 80 job-description)])
    (when posted-at?
      [:div.posted-at.text-right
       {:style "font-size:10px;font-weight:bold;text-transform:uppercase;color:#aaa;margin-top:3px"}
       "Posted " (util/timeago posted-at) " ago"])
    ]])

(defn list-handler [{:keys [params uri user]}]
  (common/$main
    {:body-class "jobs-page"
     :title (str "Jobs | ClojureDocs - Community-Powered Clojure Documentation and Examples")
     :user user
     :page-uri uri
     :content
     [:div.row
      [:div.col-md-12
       [:div.list-jobs-header
        {:style "border-bottom:solid #ccc 1px;padding-bottom:5px;margin:0;position:relative;"}
        [:h2 {:style "text-align:center;margin:0"}
         "Latest Jobs"]
        [:div {:style "position:absolute;bottom:5px;right:0;font-size:12px"}
         [:a {:href "/jobs/post"} "Post a Job"]]]
       (->> DATA
            (map (fn [job]
                   [:div
                    {:style "border-bottom:solid #ccc 1px;padding:20px 0;"}
                    ($job-preview job
                      {:description? true
                       :comp? true
                       :posted-at? true})])))]]}))

(defn find-job [job-id]
  (->> DATA
       (filter #(= job-id (:short-id %)))
       first))

(defn single-handler [job-id]
  (fn [{:keys [params uri user]}]
    (let [job (find-job job-id)]
      (when job
        (common/$main
          {:body-class "jobs-page"
           :title (str "Jobs | ClojureDocs - Community-Powered Clojure Documentation and Examples")
           :user user
           :page-uri uri
           :meta {:robots "noindex"}
           :content
           [:div.row
            [:div.col-md-10.col-md-offset-1
             (let [{:keys [job-description company-description
                           company-name
                           job-apply-href]} job]
               [:div.job-info
                {:style "border-top:solid #ccc 1px;padding:20px 0px"}
                [:div
                 {:style "position:relative;"}
                 ($job-preview job)
                 [:a.btn.btn-success
                  {:href job-apply-href
                   :target "_blank"
                   :style "position:absolute;top:0px;right:0px"}
                  "Apply Now"]]
                [:br]
                [:div.job-description
                 [:h2.section-header "Job Description"]
                 (util/markdown job-description)]
                [:br]
                [:div.company-description
                 [:h2.section-header "About " company-name]
                 (util/markdown company-description)]
                [:br]
                [:div.apply-now
                 [:a.btn.btn-success.btn-lg
                  {:href job-apply-href
                   :target "_blank"}
                  "Apply Now"]]])]]})))))

(defn post-handler [{:keys [params uri user]}]
  (common/$main
    {:body-class "post-job-page"
     :title (str "Post a Jobs | ClojureDocs - Community-Powered Clojure Documentation and Examples")
     :user user
     :page-uri uri
     :content
     [:div#page-content]}))
