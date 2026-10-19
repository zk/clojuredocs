(ns clojuredocs.api.examples-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojuredocs.api.examples :as examples]
            [clojuredocs.data :as data]
            [slingshot.slingshot :refer [try+]]))

(def ^:private example-id "507f1f77bcf86cd799439011")

(def ^:private var-ref
  {:ns "clojure.core" :name "map" :library-url "https://github.com/clojure/clojure"})

(def ^:private author
  {:login "alice" :account-source "github" :avatar-url "http://example.com/alice.png"})

(def ^:private non-author
  {:login "bob" :account-source "github" :avatar-url "http://example.com/bob.png"})

(def ^:private stored-example
  {:_id (org.bson.types.ObjectId. example-id)
   :var var-ref
   :body "original body"
   :author author
   :editors []
   :created-at 1000
   :updated-at 1000})

(defn- unreachable [msg]
  (fn [& _] (throw (ex-info (str msg " must not be reached") {}))))

;; -- post-example-handler --

(deftest post-example-rejects-anonymous-users
  (testing "an anonymous request is rejected before touching the database"
    (with-redefs [data/insert-example! (unreachable "insert-example!")]
      (try+
        (examples/post-example-handler {:edn-body {:var var-ref :body "an example"} :user nil})
        (is false "expected a 401 rejection")
        (catch [:status 401] _ (is true))))))

(deftest post-example-rejects-blank-body
  (testing "a blank example body is rejected with a helpful message"
    (with-redefs [data/insert-example! (unreachable "insert-example!")]
      (try+
        (examples/post-example-handler {:edn-body {:var var-ref :body ""} :user author})
        (is false "expected a 422 rejection")
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)blank" (:message body))))))))

(deftest post-example-rejects-missing-var
  (testing "an example with no var is rejected"
    (with-redefs [data/insert-example! (unreachable "insert-example!")]
      (try+
        (examples/post-example-handler {:edn-body {:body "an example"} :user author})
        (is false "expected a 422 rejection")
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)var" (:message body))))))))

(deftest post-example-inserts-a-well-formed-example
  (testing "a valid example is stamped with author/timestamps and persisted"
    (let [inserted (atom nil)]
      (with-redefs [data/insert-example! (fn [payload] (reset! inserted payload) payload)]
        (let [resp (examples/post-example-handler {:edn-body {:var var-ref :body "an example"} :user author})]
          (is (= 200 (:status resp)))
          (is (= author (:author @inserted)))
          (is (= "an example" (:body @inserted)))
          (is (number? (:created-at @inserted)))
          (is (number? (:updated-at @inserted))))))))

;; -- patch-example-handler --

(deftest patch-example-requires-login
  (testing "an anonymous request is rejected"
    (with-redefs [data/find-example-by-id (unreachable "find-example-by-id")]
      (try+
        ((examples/patch-example-handler example-id) {:edn-body {:body "edit"} :user nil})
        (is false "expected a 401 rejection")
        (catch [:status 401] _ (is true))))))

(deftest patch-example-missing-returns-422
  (testing "patching an example that doesn't exist fails with a clear error, not an NPE"
    (with-redefs [data/find-example-by-id (fn [& _] nil)
                  data/update-example! (unreachable "update-example!")
                  data/insert-example-history! (unreachable "insert-example-history!")]
      (try+
        ((examples/patch-example-handler example-id) {:edn-body {:body "edit"} :user author})
        (is false "expected a 422 rejection")
        (catch [:status 422] {:keys [body]}
          (is (re-find #"(?i)couldn.t find" (:error body))))))))

(deftest patch-example-updates-body-and-adds-editor
  (testing "a valid patch updates the body, records the editor, and writes a history entry"
    (let [updated (atom nil)
          history (atom nil)]
      (with-redefs [data/find-example-by-id (fn [_id] stored-example)
                    data/update-example! (fn [_id example] (reset! updated example) example)
                    data/insert-example-history! (fn [h] (reset! history h) h)]
        (let [resp ((examples/patch-example-handler example-id)
                    {:edn-body {:body "edited body"} :user non-author})]
          (is (= 200 (:status resp)))
          (is (= "edited body" (:body @updated)))
          (is (some #(= (select-keys % [:login :account-source])
                        (select-keys non-author [:login :account-source]))
                    (:editors @updated))
              "the editor should be recorded on the example")
          (is (= "edited body" (:body @history)))
          (is (= (:_id stored-example) (:example-id @history))))))))

;; -- delete-example-handler --

(deftest delete-example-requires-login
  (testing "an anonymous request is rejected"
    (with-redefs [data/find-example-by-id (unreachable "find-example-by-id")]
      (try+
        ((examples/delete-example-handler example-id) {:user nil})
        (is false "expected a 401 rejection")
        (catch [:status 401] _ (is true))))))

(deftest delete-example-missing-returns-404
  (testing "deleting an example that doesn't exist returns 404"
    (with-redefs [data/find-example-by-id (fn [& _] nil)
                  data/update-example! (unreachable "update-example!")]
      (try+
        ((examples/delete-example-handler example-id) {:user author})
        (is false "expected a 404 rejection")
        (catch [:status 404] _ (is true))))))

(deftest delete-example-rejects-non-author
  (testing "a logged-in user who is not the author cannot delete the example"
    (with-redefs [data/find-example-by-id (fn [& _] stored-example)
                  data/update-example! (unreachable "update-example!")]
      (try+
        ((examples/delete-example-handler example-id) {:user non-author})
        (is false "expected a 401 rejection")
        (catch [:status 401] {:keys [body]}
          (is (re-find #"(?i)not authorized" (:message body))))))))

(deftest delete-example-soft-deletes-as-author
  (testing "the author can soft-delete the example (sets deleted-at rather than removing it)"
    (let [updated (atom nil)]
      (with-redefs [data/find-example-by-id (fn [& _] stored-example)
                    data/update-example! (fn [_id example] (reset! updated example) example)]
        (let [resp ((examples/delete-example-handler example-id) {:user author})]
          (is (= 200 (:status resp)))
          (is (some? (:deleted-at @updated))))))))
