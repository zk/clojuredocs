(in-ns 'user)

(require '[nsfw.reup])

(def reup
  (nsfw.reup/setup
    {:start-app-sym 'clojuredocs.main/start-app
     :stop-app-sym 'clojuredocs.main/stop-app
     :tests-regex #"clojuredocs.*-test"}))

(reup)
