(ns clojuredocs.mail
  (:require [clj-http.client :as client]
            [clojuredocs.config :as config]
            [somnium.congomongo :as mon]))

(defn migrate-account-content [migration-key]
  (format
    "Hey There,

  You're receiving this message because somebody (probably you) requested that we migrate your ClojureDocs account. You can do this by visiting the following link:

  %s

  If you didn't request this email, you can safely ignore it.

  Thanks!"
    (config/url "/migrate-account/migrate/" migration-key)))

(defn migration-request [to-email migration-key]
  (let [{:keys [endpoint api-key from]} config/mailgun-config]
    {:method :post
     :url endpoint
     :basic-auth ["api" api-key]
     :form-params {:from from
                   :to to-email
                   :subject "Migrate Your ClojureDocs Account"
                   :text (migrate-account-content migration-key)}}))

(defn send-email [payload]
  (let [res (client/request payload)]
    (mon/insert! :events
      {:tag "email-sent"
       :payload (assoc payload :basic-auth "REDACTED")
       :response res})))
