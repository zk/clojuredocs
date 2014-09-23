(ns clojuredocs.config
  (:require [clojuredocs.env :as env]))

(def env-vars
  [{:key :gh-client-id
    :type :string
    :doc "GitHub application client id"
    :required? true}

   {:key :gh-client-secret
    :type :string
    :doc "GitHub application secret key"
    :required? true}

   {:key :base-url
    :type :string
    :doc "Base URL of app, used to construct fully-qualified urls to the app (emails, etc)."
    :required? true}

   {:key :debug-exceptions
    :type :bool
    :doc "Render debug info to browser?"
    :required? true}

   {:key :session-key
    :type :string
    :doc "String key used for encrypting the session (stored in cookie)"
    :required? true}

   {:key :staging-banner
    :type :bool
    :doc "Show staging banner at top of pages?"
    :required? true}

   {:key :cljs-dev
    :type :bool
    :doc "Include dev cljs deps?"
    :required? false
    :default false}

   {:key :mongo-url
    :type :string
    :doc "'mongodb://'-style url"
    :required? true}

   {:key :allow-robots
    :type :bool
    :doc "Deny-all in robots.txt?"
    :required? true}

   {:key :log-exceptions
    :type :bool
    :doc "Log exceptions to console?"
    :required? false
    :default false}

   {:key :mailgun-api-key
    :type :string
    :doc "Mailgun API key"
    :required? true}

   {:key :mailgun-api-endpoint
    :type :string
    :doc "Mailgun message endpoint (including domain to send from)."
    :required? true}

   {:key :ga-tracking-id
    :type :string
    :doc "GA id (UA-...)"
    :required? false
    :default "UA-17348828-3"}

   {:key :cache-markdown
    :type :bool
    :doc "Cache markdown from disk for duration of app process?"
    :required? false
    :default false}

   {:key :from-email
    :type :string
    :doc "Email address to put in 'from' field "
    :default "ClojureDocs Development <dev@clojuredocs.org>"}

   {:key :new-relic-app-name
    :type :string
    :doc "App name for display in new relic, ex `cd-dev`, `cd-staging`"
    :required? false}

   {:key :new-relic-license-key
    :type :string
    :doc "New Relic license key"
    :required? false}])

(defn get-env [lookup key]
  (let [{:keys [key type doc required? default] :as env-var-schema}
        (->> lookup
             (filter #(= key (:key %)))
             first)
        f (condp = type
            :int env/int
            :bool env/bool
            env/str)]
    (when-not env-var-schema
      (throw (Exception. (str "Can't find description of env var " key))))
    (let [res (f key)]
      (if (not (nil? res))
        res
        default))))

(defn resolve-env [env-vars]
  (->> env-vars
       (map #(assoc % :value (get-env env-vars (:key %))))))

(def missing-env-vars
  (->> env-vars
       resolve-env
       (filter #(and (:required? %) (nil? (:value %))))))

(def gh-creds {:client-id (get-env env-vars :gh-client-id)
               :client-secret (get-env env-vars :gh-client-secret)})

(def base-url (get-env env-vars :base-url))

(def staging-banner? (get-env env-vars :staging-banner))

(def ga-tracking-id (get-env env-vars :ga-tracking-id))

(def cljs-dev? (get-env env-vars :cljs-dev))

(def allow-robots? (get-env env-vars :allow-robots))

(defn url [& s] (apply str base-url s))

(def cache-markdown? (get-env env-vars :cache-markdown))

(def debug-exceptions? (get-env env-vars :debug-exceptions))

(def log-exceptions? (get-env env-vars :log-exceptions))

(def mailgun-api-key (get-env env-vars :mailgun-api-key))
(def mailgun-api-endpoint (get-env env-vars :mailgun-api-endpoint))

(def from-email (get-env env-vars :from-email))

(def mailgun-config
  {:endpoint mailgun-api-endpoint
   :api-key mailgun-api-key
   :from from-email})
