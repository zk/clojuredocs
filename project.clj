(defproject clojuredocs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "target/generated/clj"]
  :test-paths ["test/clj"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [aleph "0.4.0"]
                 [prismatic/schema "0.2.6"]
                 [hiccup "1.0.4"]
                 [prismatic/dommy "0.1.2"]
                 [org.clojure/clojurescript "0.0-3058"]
                 [clucy "0.4.0"]
                 [watchtower "0.1.1"]
                 [clj-http "1.1.2"]
                 [cheshire "5.2.0"]
                 [org.clojure/java.jdbc "0.3.0-beta2"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [congomongo "0.4.1"]
                 [unk "0.9.1"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/core.logic "0.8.8"]
                 [om "0.6.4"]
                 [prismatic/om-tools "0.2.2"
                  :exclusions [org.clojure/clojure]]
                 [org.pegdown/pegdown "1.4.2"]
                 [sablono "0.2.21"]
                 [clj-fuzzy "0.1.8"]
                 [slingshot "0.10.3"]
                 [prone "0.6.0"]]
  :java-agents [[com.newrelic.agent.java/newrelic-agent "3.10.0"]]
  :repl-options {:init (do
                         (require 'clojuredocs.main)
                         (-> (clojuredocs.main/create-app)
                             clojuredocs.main/start))}
  :plugins [[lein-cljsbuild "1.0.3"]
            ;; required for heroku deploy
            [com.keminglabs/cljx "0.6.0" :exclusions [org.clojure/clojure]]]
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/generated/clj"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/generated/cljs"
                   :rules :cljs}]}
  :cljsbuild {:builds
              {:dev  {:source-paths ["src/cljs" "target/generated/cljs"]
                      :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                 :output-dir "resources/public/cljs"
                                 :optimizations :none
                                 :source-map true
                                 :externs ["externs/morpheus.js"]}}

               ;; for debugging advanced compilation problems
               :dev-advanced  {:source-paths ["src/cljs" "target/generated/cljs"]
                               :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                          :output-dir "resources/public/cljs-advanced"
                                          :source-map "resources/public/cljs/clojuredocs.js.map"
                                          :optimizations :advanced
                                          :preamble ["public/js/morpheus.min.js"
                                                     "react/react.min.js"
                                                     "public/js/marked.min.js"
                                                     "public/js/fastclick.min.js"]
                                          :externs ["externs/react.js"
                                                    "externs/morpheus.js"
                                                    "externs/marked.js"
                                                    "externs/fastclick.js"]}}

               :prod {:source-paths ["src/cljs" "target/generated/cljs"]
                      :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                 :optimizations :advanced
                                 :pretty-print false
                                 :preamble ["public/js/morpheus.min.js"
                                            "react/react.min.js"
                                            "public/js/marked.min.js"
                                            "public/js/fastclick.min.js"]
                                 :externs ["externs/react.js"
                                           "externs/morpheus.js"
                                           "externs/marked.js"
                                           "externs/fastclick.js"]}
                      :jar true}}})
