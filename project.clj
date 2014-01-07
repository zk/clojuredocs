(defproject clojuredocs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [aleph "0.3.0-rc2"]
                 [hiccup "1.0.4"]
                 [prismatic/dommy "0.1.2"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [clucy "0.4.0"]
                 [watchtower "0.1.1"]
                 [org.clojure/tools.reader "0.7.10"]
                 [clj-http "0.7.7"]
                 [cheshire "5.2.0"]
                 [org.clojure/java.jdbc "0.3.0-beta2"]
                 [mysql/mysql-connector-java "5.1.25"]
                 #_[lein-light-nrepl "0.0.9"]
                 [congomongo "0.4.1"]
                 [unk "0.9.1"]
                 [cljsbuild "1.0.1"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/core.logic "0.8.5"]]
  :profiles {:nrepl {:source-paths ["dev"]
                     :dependencies [[org.clojure/tools.namespace "0.2.4"]]
                     :repl-options {:init (user/restart)}}}
  :repl-options {:port 7888
                 :nrepl-middleware [#_lighttable.nrepl.handler/lighttable-ops]}
  :plugins [[lein-cljsbuild "1.0.0"]] ;; required for heroku deploy
  :cljsbuild {:builds
              {:dev  {:source-paths ["src/cljs"]
                      :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                 :output-dir "resources/public/cljs"
                                 :optimizations :none
                                 :source-map true
                                 :foreign-libs [{:file "js/syntaxhighlighter.js"
                                                 :provides ["highlight"]}]
                                 :externs ["externs/syntaxhighlighter.js"]}}

               ;; for debugging advanced compilation problems
               :dev-advanced  {:source-paths ["src/cljs"]
                               :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                          :output-dir "resources/public/cljs-advanced"
                                          :source-map "resources/public/cljs/clojuredocs.js.map"
                                          :optimizations :advanced}}

               :prod {:source-paths ["src/cljs"]
                      :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                 :optimizations :advanced
                                 :pretty-print false}
                      :jar true}}})
