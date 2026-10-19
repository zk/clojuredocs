(ns clojuredocs.api.see-alsos-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojuredocs.api.see-alsos :as see-alsos]
            [clojuredocs.data :as data]
            [clojuredocs.search :as search]
            [slingshot.slingshot :refer [try+]])
  (:import [org.bson.types ObjectId]))

(def ^:private see-also-id "507f1f77bcf86cd799439011")

(def ^:private from-var
  {:ns "clojure.core" :name "map" :library-url "https://github.com/clojure/clojure"})

(def ^:private to-var
  {:ns "clojure.core" :name "filter" :library-url "https://github.com/clojure/clojure"})

(def ^:private author
  {:login "alice" :account-source "github" :avatar-url "http://example.com/alice.png"})

(def ^:private non-author
  {:login "bob" :account-source "github" :avatar-url "http://example.com/bob.png"})

(def ^:private stored-see-also
  {:_id (ObjectId. see-also-id)
   :from-var from-var
   :to-var to-var
   :author author
   :created-at 1000})

(defn- unreachable [msg]
  (fn [& _] (throw (ex-info (str msg " must not be reached") {}))))

;; -- post-see-also-handler --

(deftest post-see-also-rejects-anonymous-users
  (testing "an anonymous request is rejected before touching the database"
    (with-redefs [data/insert-see-also! (unreachable "insert-see-also!")]
      (try+
        (see-alsos/post-see-also-handler
          {:edn-body {:fq-to-var-name "clojure.core/filter" :from-var from-var} :user nil})
        (is false "expected a 401 rejection")
        (catch [:status 401] _ (is true))))))

(deftest post-see-also-rejects-unknown-target-var
  (testing "a see-also pointing at a var that doesn't resolve is rejected with a clear 404"
    (with-redefs [search/lookup (fn [_] nil)
                  data/insert-see-also! (unreachable "insert-see-also!")]
      (try+
        (see-alsos/post-see-also-handler
          {:edn-body {:fq-to-var-name "clojure.core/nope" :from-var from-var} :user author})
        (is false "expected a 404 rejection")
        (catch [:status 404] {:keys [body]}
          (is (re-find #"(?i)couldn.t find var" (:message body))))))))

(deftest post-see-also-rejects-self-reference
  (testing "a var can't see-also itself"
    (with-redefs [search/lookup (fn [_] from-var)
                  data/find-see-also-between (fn [_ _] nil)
                  data/insert-see-also! (unreachable "insert-see-also!")]
      (try+
        (see-alsos/post-see-also-handler
          {:edn-body {:fq-to-var-name "clojure.core/map" :from-var from-var} :user author})
        (is false "expected a 422 rejection")
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)itself" (:message body))))))))

(deftest post-see-also-rejects-duplicates
  (testing "an already-existing see-also relation is rejected"
    (with-redefs [search/lookup (fn [_] to-var)
                  data/find-see-also-between (fn [_ _] stored-see-also)
                  data/insert-see-also! (unreachable "insert-see-also!")]
      (try+
        (see-alsos/post-see-also-handler
          {:edn-body {:fq-to-var-name "clojure.core/filter" :from-var from-var} :user author})
        (is false "expected a 422 rejection")
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)already exists" (:message body))))))))

(deftest post-see-also-inserts-a-well-formed-see-also
  (testing "a valid see-also is trimmed to ns/name/library-url and persisted"
    (let [inserted (atom nil)]
      (with-redefs [search/lookup (fn [_] (assoc to-var :doc "a doc"))
                    data/find-see-also-between (fn [_ _] nil)
                    data/insert-see-also! (fn [sa] (reset! inserted sa) sa)]
        (let [resp (see-alsos/post-see-also-handler
                     {:edn-body {:fq-to-var-name "clojure.core/filter" :from-var from-var} :user author})]
          (is (= 200 (:status resp)))
          (is (= from-var (:from-var @inserted)))
          (is (= to-var (:to-var @inserted)))
          (is (= author (:author @inserted)))
          (is (= "a doc" (:doc (:body resp)))))))))

;; -- delete-see-also-handler --

(deftest delete-see-also-requires-login
  (testing "an anonymous request is rejected"
    (with-redefs [data/find-see-also-by-id (unreachable "find-see-also-by-id")]
      (try+
        ((see-alsos/delete-see-also-handler see-also-id) {:user nil})
        (is false "expected a 401 rejection")
        (catch [:status 401] _ (is true))))))

(deftest delete-see-also-rejects-non-author
  (testing "a logged-in user who is not the author cannot delete the see-also"
    (with-redefs [data/find-see-also-by-id (fn [& _] stored-see-also)
                  data/delete-see-also! (unreachable "delete-see-also!")]
      (try+
        ((see-alsos/delete-see-also-handler see-also-id) {:user non-author})
        (is false "expected a 422 rejection")
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)can.t delete" (:message body))))))))

(deftest delete-see-also-removes-as-author
  (testing "the author can delete the see-also"
    (let [deleted-id (atom nil)]
      (with-redefs [data/find-see-also-by-id (fn [& _] stored-see-also)
                    data/delete-see-also! (fn [_id] (reset! deleted-id _id))]
        (let [resp ((see-alsos/delete-see-also-handler see-also-id) {:user author})]
          (is (= 200 (:status resp)))
          (is (= stored-see-also (:body resp)))
          (is (= (:_id stored-see-also) @deleted-id)))))))
