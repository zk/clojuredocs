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

(defn add-indexes-to-coll! [coll ks]
  (doseq [k ks]
    (mon/add-index! :examples [k])))

(defn add-all-indexes! []
  (add-indexes-to-coll!
    :examples [:var :deleted-at
               :author.login :author.account-source
               :editors.login :editors.account-source])

  (add-indexes-to-coll! :namespaces [:name])

  (add-indexes-to-coll!
    :see-alsos [:from-var.name :from-var.ns :from-var.library-url
                :to-var.ns :to-var.name :to-var.library-url
                :account.login :account.account-source])

  (add-indexes-to-coll! :libraries [:namespaces])

  (add-indexes-to-coll!
    :notes [:var.ns :var.name :var.library-url
            :account.login :account.account-source])

  (add-indexes-to-coll!
    :legacy-var-redirects [:function-id
                           :editor.login :editor.account-source])

  (add-indexes-to-coll! :users [:login :account-source])

  (add-indexes-to-coll! :migrate-users [:email :migraion-key]))

(defn start-app []
  (let [{:keys [mongo-url port entry] :as app} (create-app)]
    (report-and-exit-on-missing-env-vars!)
    (mon/set-connection! (mon/make-connection mongo-url))
    (add-all-indexes!)
    (let [stop-server (start-http-server entry
                        {:port port :join? false})]
      (println (format "Server running on port %d" port))
      (fn []
        (.stop stop-server)))))

(defn stop-app [f]
  (prn "STOP APP" f)
  (when f (f)))

(defn -main []
  (start-app))
