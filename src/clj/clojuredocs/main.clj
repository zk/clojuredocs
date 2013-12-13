(ns clojuredocs.main
  (:require [ring.adapter.jetty :as jetty]
            [aleph.http :as ah]
            [somnium.congomongo :as mon]
            [clojuredocs.env :as env]
            [clojuredocs.entry :as entry]
            [clojuredocs.config :as config]))

(mon/set-connection!
  (mon/make-connection (env/str :mongo-url)))

(mon/add-index! :examples [:ns :name :library-url])
(mon/add-index! :vars [:ns :name :library-url])

(defn start-http-server [entry-point opts]
  (ah/start-http-server
    (ah/wrap-ring-handler
      (fn [r]
        (let [resp (entry-point r)]
          (if (:status resp)
            resp
            (assoc resp :status 200)))))
    opts))

(defn valid-env-or-exit []
  (let [res (->> [(env/int :port)                  "Port missing"
                  (:client-id config/gh-creds)     "GH client ID missing"
                  (:client-secret config/gh-creds) "GH client secret missing"
                  config/base-url                  "base url missing"]
                 (partition 2)
                 (map #(when (nil? (first %))
                         (println " !" (second %))
                         true))
                 (reduce #(or %1 %2)))]
    (when res
      (println)
      (println " ! Missing required config vars, exiting.")
      (println)
      #_(System/exit 1))))

(defn -main []
  (valid-env-or-exit)
  (let [port (env/int :port 8080)]
    (start-http-server
      (var entry/routes)
      {:port port :join? false})
    (println (format "Server running on port %d" port))))
