(ns tools.sanity-check
  (:require [clojure.java.jdbc :as j]
            [clojure.set :as set]
            [somnium.congomongo :as mon]))

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/clojuredocs"
               :user "root"
               :password ""})

(defn all-users []
  (j/query mysql-db ["SELECT * FROM users"]))

(defn clojure-core-lib []
  (first (j/query mysql-db ["SELECT * FROM libraries WHERE id=3"])))

(defn clojure-core-lib []
  (first (j/query mysql-db ["SELECT * FROM libraries WHERE id=3"])))

(defn core-nss []
  (j/query mysql-db ["SELECT * FROM namespaces WHERE library_id=3 OR library_id=15"]))

(defn core-functions []
  (let [ns-ids (map :id (core-nss))]
    (j/query mysql-db [(format "SELECT * FROM functions WHERE namespace_id IN (%s)"
                         (->> ns-ids
                              (interpose ",")
                              (apply str)))])))

(defn core-examples []
  (let [fids (map :id (core-functions))]
    (j/query mysql-db [(format "SELECT * FROM examples WHERE function_id IN (%s)"
                         (->> fids
                              (interpose ",")
                              (apply str)))])))

(defn core-see-alsos []
  (let [fs (core-functions)
        fids (map :id fs)
        comma-sep (->> fids
                       (interpose ",")
                       (apply str))
        sas (j/query mysql-db
              [(format "SELECT * FROM see_alsos WHERE from_id IN (%s)"
                 comma-sep
                 comma-sep)])]
    sas))

(defn core-notes []
  (let [fs (core-functions)
        fids (map :id fs)
        comma-sep (->> fids
                       (interpose ",")
                       (apply str))
        notes (j/query mysql-db
                [(format "SELECT * FROM comments WHERE commentable_id IN (%s)"
                   comma-sep)])]
    notes))

#_(->> (core-functions)
     (map :id)
     (mapcat #(j/query mysql-db
                [(format "SELECT * FROM comments WHERE commentable_id=%s"
                   %)]))
     (remove empty?)
     count)

(defn all-functions []
  (j/query mysql-db ["SELECT * FROM functions"]))

(defn all-notes []
  (j/query mysql-db ["SELECT * FROM comments"]))

(defn process-notes [lookup ns]
  (->> ns
       (map (fn [{:keys [id body commentable_id]}]
              (let [{:keys [name file] :as l} (lookup commentable_id)]
                (when l
                  {:id id
                   :body body
                   :name name
                   :file file}))))
       (sort-by :file)
       (remove nil?)))


(defn render-note-overview [{:keys [file name body]}]
  (format "%s -- %s   %s"
    file
    name
    (->> body
         (take 50)
         (apply str)
         pr-str)))

#_(let [cfs (core-functions)
      afs (all-functions)
      lookup (reduce #(assoc %1 (:id %2) %2) {} afs)
      cnotes (core-notes)
      anotes (all-notes)
      cnotes-prime (process-notes lookup cnotes)
      anotes-prime (process-notes lookup anotes)]
  (doseq [n (set/difference (set anotes-prime) (set cnotes-prime))]
    (println (render-note-overview n)))
  (println "CORE NOTES:" (count cnotes-prime))
  (println "ALL NOTES:" (count anotes-prime)))


(do
  (println "-- Core Examples")
  (println "MySQL:" (count (core-examples)))
  (println "Mongo:" (count (mon/fetch :examples))))


(do
  (println "-- Users")
  (println "MySQL:" (count (all-users)))
  (println "Mongo:" (count (mon/fetch :users)))
  (println "Duplicate usernames:" (->> (all-users)
                                       (map :login)
                                       (reduce #(assoc %1 %2 (inc (%1 %2 0))) {})
                                       (sort-by second)
                                       (filter #(> (second %) 1)))))

(do
  (println "-- See Alsos")
  (println "MySQL:" (count (core-see-alsos)))
  (println "Mongo:" (count (mon/fetch :see-alsos))))

(do
  (println "-- Notes")
  (println "MySQL:" (count (core-notes)))
  (println "Mongo:" (count (mon/fetch :notes))))
