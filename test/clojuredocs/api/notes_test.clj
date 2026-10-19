(ns clojuredocs.api.notes-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojuredocs.api.notes :as notes]
            [clojuredocs.data :as data]
            [slingshot.slingshot :refer [try+]]))

(def ^:private note-id "507f1f77bcf86cd799439011")

(def ^:private stored-note
  {:_id (org.bson.types.ObjectId. note-id)
   :body "original body"
   :var {:ns "clojure.core" :name "map"
         :library-url "https://github.com/clojure/clojure"}
   :author {:login "alice" :account-source "github"
            :avatar-url "http://example.com/alice.png"}
   :created-at 1000
   :updated-at 1000})

(def ^:private non-author
  {:login "bob" :account-source "github"
   :avatar-url "http://example.com/bob.png"})

(deftest patch-note-rejects-non-author
  (testing "a logged-in user who is not the author cannot edit the note"
    (with-redefs [data/find-note-by-id (fn [& _] stored-note)
                  data/update-note! (fn [& _]
                                       (throw (ex-info "update-note! must not be reached for a non-author" {})))]
      (try+
        (let [resp ((notes/patch-note-handler note-id)
                    {:edn-body {:body "malicious edit"} :user non-author})]
          (is false (str "expected a 422 authorization rejection, got status "
                         (:status resp))))
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)author" (:message body))
              "rejection message should explain the author requirement"))))))

(deftest patch-note-allows-author
  (testing "the note's own author can edit it (guards against over-restriction)"
    (let [updated (atom nil)]
      (with-redefs [data/find-note-by-id (fn [& _] stored-note)
                    data/update-note! (fn [_id new-note] (reset! updated new-note))]
        (let [author (:author stored-note)
              resp ((notes/patch-note-handler note-id)
                    {:edn-body {:body "an authorized edit"} :user author})]
          (is (= 200 (:status resp)))
          (is (= "an authorized edit" (:body (:body resp))))
          (is (= "an authorized edit" (:body @updated))
              "the edited body should be persisted"))))))

(deftest patch-note-missing-returns-404
  (testing "patching a note that does not exist returns 404, not a misleading 422"
    (with-redefs [data/find-note-by-id (fn [& _] nil)
                  data/update-note! (fn [& _]
                                       (throw (ex-info "update-note! must not be reached for a missing note" {})))]
      (try+
        (let [resp ((notes/patch-note-handler note-id)
                    {:edn-body {:body "edit"} :user non-author})]
          (is false (str "expected a 404, got status " (:status resp))))
        (catch [:status 404] {:keys [body]}
          (is (re-find #"(?i)not found" (:message body))
              "404 message should say the note was not found"))))))
