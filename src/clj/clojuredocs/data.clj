(ns clojuredocs.data
  (:require [somnium.congomongo :as mon]))

(defn connect!
  [mongo-url]
  (let [conn (mon/make-connection mongo-url)]
    (mon/set-connection! conn)
    conn))

(defn disconnect! [conn]
  (mon/close-connection conn))

(defn add-index! [coll ks]
  (mon/add-index! coll ks))

;; Users

(defn upsert-user! [user]
  (mon/update! :users
    {:login (:login user)
     :account-source (:account-source user)}
    user))

;; Examples

(defn find-examples-for [{:keys [ns name library-url]}]
  (mon/fetch :examples
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url
            :deleted-at nil}
    :sort {:created-at 1}))

(defn count-examples-for [{:keys [ns name library-url]}]
  (mon/fetch-count :examples
    :where {:var.ns ns
            :var.name name
            :var.library-url library-url
            :deleted-at nil}))

(defn find-example-by-id [id]
  (mon/fetch-one :examples :where {:_id id}))

(defn insert-example! [example]
  (mon/insert! :examples example)
  example)

(defn update-example! [id example]
  (mon/update! :examples {:_id id} example)
  example)

;; Example histories

(defn insert-example-history! [example-history]
  (mon/insert! :example-histories example-history)
  example-history)

(defn find-recent-examples [limit]
  (mon/fetch :examples
    :where {:deleted-at nil}
    :sort {:created-at -1}
    :limit limit))

(defn count-examples-by-author [login account-source]
  (mon/fetch-count :examples
    :where {:author.login login
            :author.account-source account-source
            :deleted-at nil}))

(defn find-examples-by-author [login account-source]
  (mon/fetch :examples
    :where {:author.login login
            :author.account-source account-source
            :deleted-at nil}))

;; Notes

(defn find-notes-for [{:keys [ns name library-url]}]
  (mon/fetch :notes
    :where {:var.ns ns
            :var.name name
            :var.library-url library-url}
    :sort {:created-at 1}))

(defn find-recent-notes [limit]
  (mon/fetch :notes :sort {:created-at -1} :limit limit))

(defn insert-note! [note]
  (mon/insert! :notes note)
  note)

(defn find-note-by-id [id]
  (mon/fetch-one :notes :where {:_id id}))

(defn update-note! [id note]
  (mon/update! :notes {:_id id} note)
  note)

(defn delete-note! [id]
  (mon/destroy! :notes {:_id id}))

;; See Alsos

(defn find-see-alsos-for [{:keys [ns name library-url]}]
  (mon/fetch :see-alsos
    :where {:from-var.name name
            :from-var.ns ns
            :from-var.library-url library-url}))

(defn find-all-see-alsos []
  (mon/fetch :see-alsos))

(defn find-recent-see-alsos [limit]
  (mon/fetch :see-alsos :sort {:created-at -1} :limit limit))

(defn find-see-also-between [from-var to-var]
  (mon/fetch-one :see-alsos :where {:from-var from-var :to-var to-var}))

(defn insert-see-also! [see-also]
  (mon/insert! :see-alsos see-also)
  see-also)

(defn find-see-also-by-id [id]
  (mon/fetch-one :see-alsos :where {:_id id}))

(defn delete-see-also! [id]
  (mon/destroy! :see-alsos {:_id id}))

;; Vars

(defn fetch-var [ns name]
  (mon/fetch-one :vars :where {:name name :ns ns}))

;; Users

(defn find-user [login account-source]
  (mon/fetch-one :users :where {:login login :account-source account-source}))

;; Legacy redirects

(defn find-legacy-var-redirect [function-id]
  (mon/fetch-one :legacy-var-redirects :where {:function-id function-id}))
