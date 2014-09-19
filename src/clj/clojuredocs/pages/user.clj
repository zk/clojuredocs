(ns clojuredocs.pages.user
  (:require [clojuredocs.util :as util]
            [clojuredocs.pages.common :as common]
            [clojuredocs.mail :as mail]
            [somnium.congomongo :as mon]
            [ring.util.response :refer (redirect)]
            [compojure.core :refer (defroutes GET)]))

(defn page-handler [login account-source]
  (let [{:keys [login account-source] :as user}
        (mon/fetch-one :users :where {:login login :account-source account-source})

        examples-authored-count
        (mon/fetch-count :examples :where {:author.login login
                                           :author.account-source account-source
                                           :deleted-at nil})]
    (fn [r]
      (common/$main
        {:user (:user r)
         :page-uri (:uri r)
         :body-class "user-page"
         :content [:div.row
                   [:div.col-sm-10.col-sm-offset-1
                    [:div.row
                     [:div.col-sm-4
                      [:span.user-avatar
                       (util/$avatar user {:size 200})]]
                     [:div.col-sm-8
                      [:h1 login]
                      [:p "User " [:b login] " has authored " examples-authored-count " examples."]]]]]}))))


(defn migrate-account-handler [{:keys [user]}]
  (common/$main
    {:user user
     :content [:div.row
               [:div.col-sm-10.col-sm-offset-1
                [:h1 "Migrate your old ClojureDocs account"]
                [:p "One of the mistakes we made with ClojureDocs v1 was maintaining user accounts and identities on-property. The last thing you need is to remember a new login / password combination, and we want as little as possible of your personal information in our database as possible."]
                [:p "Therefore, we're going to rely on 3rd party services to provide authentication for the site, specifically " [:a {:href "https://developer.github.com/v3/oauth/"} "GitHub Auth"] ", which will solve the problems above, and, bonus, make it easier to maintain the site."]
                [:h2 "How it Works"]
                [:p "Enter the email address associated with your ClojureDocs account below, and we'll send an email to that account with instructions on how to associate your old ClojureDocs data (Examples, See-Alsos, and Notes), with your GitHub login."]
                [:form
                 {:method "POST" :action "/migrate-account/send-email"}
                 [:div.form-group
                  [:label {:for "email"} "Old ClojureDocs Account Email Address"]
                  [:input.form-control {:name "email"}]]
                 [:div.form-group
                  [:button.btn.btn-success.pull-right "Send Email"]]]
                [:div {:style "clear: both;"}]
                [:br]
                [:p "If you're not sure what the email associated with your old ClojureDocs account is, send an email to "
                 [:a {:href "mailto:accountmigration@clojuredocs.org"} "accountmigration@clojuredocs.org"]
                 "."]]]}))

(defn send-email-handler [{:keys [params user]}]
  (let [email (:email params)
        migration-key (util/uuid)
        expires-at (+ (util/now) (* 1000 60 60 24))
        migrate-user (mon/fetch-one :migrate-users :where {:email email})]
    (when migrate-user
      (mon/update! :migrate-users
        {:_id (:_id migrate-user)} (assoc migrate-user
                                     :migration-key migration-key
                                     :expires-at expires-at))
      (mail/send-email (mail/migration-request email migration-key)))
    (common/$main
      {:user user
       :content [:div.row
                 [:div.col-sm-10.col-sm-offset-1
                  (cond
                    (empty? email) [:div
                                    [:h3 "Whoops, looks like you didn't enter an email, please go back and try again."]]
                    :else [:div
                           [:h3 "Ok, if your email is in our system, we've sent an email to "
                            [:b email]
                            ". Please check your inbox and follow the instructions on how to migrate your old ClojureDocs account."]])]]})))

(defn migrate-account-migrate-handler [migration-key]
  (fn [{:keys [user uri]}]
    (let [{:keys [email] :as migration-user}
          (mon/fetch-one :migrate-users :where {:migration-key migration-key})]
      (common/$main
        {:user user
         :uri uri
         :body-class "migrate-user-page"
         :content
         [:div.row
          [:div.col-sm-10.col-sm-offset-1
           (if-not migration-user
             [:h3 "Sorry, we couldn't find your account to migrate."]
             (if user
               [:div
                [:h3 "Migrate Your Old Account: Confirm GitHub Account"]
                [:p
                 "Please confirm you'd like to associate the ClojureDocs account at email "
                 [:b email]
                 " to your GitHub account with login "
                 [:b (common/$avatar user) " " (:login user)]
                 "."]
                [:p
                 [:b "You can only do this once"] ", so please make sure you're logged in to the correct GitHub account. Otherwise, log out of ClojureDocs, log in to the correct GitHub account, and revisit this page."]
                [:form
                 {:method "POST"}
                 [:div.controls
                  [:button.btn.btn-success
                   {:type "submit"}
                   "Confirm Migration"]]]]
               [:div
                [:h3 "Migrate Your Old Account: Log In To GitHub"]
                [:p "Please log in with the GitHub account you'd like to associate your old ClojureDocs account's content with."]
                [:div.controls
                 [:a.btn.btn-default
                  {:href (common/gh-auth-url uri)}
                  [:i.fa.fa-github-square] " Log In Via GitHub"]]]))]]}))))

(defn post-migrate-account-migrate-handler [migration-key]
  (fn [{:keys [user uri]}]
    (if-not user
      (redirect uri)
      (let [{:keys [email login] :as migration-user}
            (mon/fetch-one :migrate-users
              :where {:migration-key migration-key})]
        (when (and user migration-user)
          (let [examples (mon/fetch :examples
                           :where {:author.login login
                                   :author.account-source "clojuredocs"})
                example-edits (mon/fetch :examples
                                :where {:editors.login login
                                        :editors.account-source "clojuredocs"})
                example-histories (mon/fetch :example-histories
                                    :where {:editor.login login
                                            :editor.account-source "clojuredocs"})
                notes (mon/fetch :notes
                        :where {:author.login login
                                :author.account-source "clojuredocs"})
                sas (mon/fetch :see-alsos
                      :where {:author.login login
                              :author.account-source "clojuredocs"})]

            (doseq [e examples]
              (mon/update! :examples
                {:_id (:_id e)}
                (assoc e :author (select-keys user [:login :account-source :avatar-url]))))

            (doseq [e example-edits]
              (mon/update! :examples
                {:_id (:_id e)}
                (assoc e
                  :editors
                  (->> e
                       :editors
                       (map (fn [ed]
                              (if (and (= login (:login ed))
                                       (= "clojuredocs" (:account-source ed)))
                                (select-keys user [:login :account-source :avatar-url])
                                ed)))))))

            (doseq [e example-histories]
              (mon/update! :example-histories
                {:_id (:_id e)}
                (assoc e :editor (select-keys user [:login :account-source :avatar-url]))))

            (doseq [n notes]
              (mon/update! :notes
                {:_id (:_id n)}
                (assoc n :author (select-keys user [:login :account-source :avatar-url]))))

            (doseq [s sas]
              (mon/update! :see-alsos
                {:_id (:_id s)}
                (assoc s :author (select-keys user [:login :account-source :avatar-url]))))

            (mon/insert! :events
              {:tag "account-migration"
               :migration-user migration-user
               :examples-count (count examples)
               :example-edits-count (count example-edits)
               :example-histories-count (count example-histories)
               :notes-count (count notes)
               :see-alsos (count sas)
               :user user
               :created-at (util/now)})

            (common/$main
              {:user user
               :content
               [:div.row
                [:div.col-sm-10.col-sm-offset-1
                 [:h1 "Migration Successful"]
                 [:p
                  "We migrated "
                  (count examples)
                  " examples authored by you, "
                  (count example-edits)
                  " examples edited by you, "
                  (count example-histories)
                  " example histories "
                  (count notes)
                  " notes, and "
                  (count sas)
                  " see alsos."]]]})))))))
