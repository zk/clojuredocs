(ns clojuredocs.main
  (:require [ring.adapter.jetty :as jetty]
            [somnium.congomongo :as mon]
            [clojuredocs.env :as env]
            [clojuredocs.entry :as entry]
            [clojuredocs.config :as config]))

(defn start-http-server [entry-point opts]
  (jetty/run-jetty
    (fn [r]
      (let [resp (entry-point r)]
        (if (:status resp)
          resp
          (assoc resp :status 200))))
    opts))

(defn create-app []
  {:port (env/int :port 8080)
   :entry #'entry/routes
   :mongo-url (env/str :mongo-url)})

(defn report-and-exit-on-missing-env-vars! []
  (when-not (empty? config/missing-env-vars)
    (println)
    (println "!!! Missing Env Vars:")
    (doseq [{:keys [key doc type]} config/missing-env-vars]
      (println "!!! " key (str "[" type "]:") doc))
    (println "!!! Exiting...")
    (println)
    (System/exit -1)))

(defn add-indexes! []
  (mon/add-index! :examples [:var
                             :deleted-at
                             :author.login :author.account-source
                             :editors.login :editors.account-source])
  (mon/add-index! :namespaces [:name])
  (mon/add-index! :see-alsos [:from-var.name :from-var.ns :from-var.library-url
                              :to-var.ns :to-var.name :to-var.library-url
                              :account.login :account.account-source])
  (mon/add-index! :libraries [:namespaces])
  (mon/add-index! :notes [:var.ns :var.name :var.library-url
                          :account.login :account.account-source])

  (mon/add-index! :legacy-var-redirects [:function-id
                                         :editor.login :editor.account-source])

  (mon/add-index! :users [:login :account-source])

  (mon/add-index! :migrate-users [:email :migraion-key]))

(defn start [{:keys [port mongo-url entry] :as opts}]
  (report-and-exit-on-missing-env-vars!)
  (mon/set-connection! (mon/make-connection mongo-url))
  #_(add-indexes!)
  (let [stop-server (start-http-server entry
                      {:port port :join? false})]
    (println (format "Server running on port %d" port))
    (merge
      opts
      {:stop-server stop-server})))

(defn stop [{:keys [stop-server] :as opts}]
  (when stop-server
    @(stop-server))
  (dissoc opts :stop-server))

(defn -main []
  (start (create-app)))
