(defproject clojuredocs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.9.946"]
                 [ring "1.5.1"]
                 [compojure "1.1.6"]
                 [aleph "0.4.2-alpha12"]
                 [clucy "0.4.0"]
                 [watchtower "0.1.1"]
                 [org.clojure/java.jdbc "0.3.0-beta2"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [unk "0.9.1"]
                 [org.clojure/core.async "1.6.681"]
                 [org.clojure/core.logic "0.8.11"]
                 [com.vladsch.flexmark/flexmark-all "0.64.8"]
                 [clj-fuzzy "0.1.8"]
                 [prone "0.6.0"]
                 [nrepl "0.6.0"]
                 [javax.xml.bind/jaxb-api "2.4.0-b180830.0359"]
                 [org.clojure/data.csv "1.0.1"]
                 [clojure-interop/java.security "1.0.5"]
                 [garden "1.2.5" :exclusions [org.clojure/clojure]]
                 [com.cognitect/transit-clj "1.0.333"]
                 [com.cognitect/transit-cljs "0.8.280"]
                 [bidi "1.23.1" :exclusions [org.clojure/clojure]]
                 [slingshot "0.12.2"]
                 [cheshire "5.5.0"]
                 [clj-http "3.6.0"]
                 [camel-snake-kebab "0.3.2"]
                 [prismatic/dommy "1.1.0"]
                 [reagent "0.6.0"]
                 [congomongo "2.6.0"]]
  :repl-options {:init (load-file "reup.clj")}
  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.18"]
            [cider/cider-nrepl "0.22.3"]]
  :cljsbuild {:builds
              {:dev  {:source-paths ["src/cljs" "src/cljc"]
                      :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                 :output-dir "resources/public/cljs"
                                 :main "clojuredocs.main"
                                 :optimizations :none
                                 :source-map true
                                 :asset-path "/cljs"
                                 :externs ["externs/morpheus.js"]}
                      :figwheel {:on-jsload "clojuredocs.main/reload-hook"}}

               ;; for debugging advanced compilation problems
               :dev-advanced  {:source-paths ["src/cljs" "src/cljc"]
                               :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                          :output-dir "resources/public/cljs-advanced"
                                          :source-map "resources/public/cljs/clojuredocs.js.map"
                                          :optimizations :advanced
                                          :preamble ["public/js/morpheus.min.js"
                                                     "public/js/marked.min.js"
                                                     "public/js/fastclick.min.js"]
                                          :externs ["externs/morpheus.js"
                                                    "externs/marked.js"
                                                    "externs/fastclick.js"]}}

               :prod {:source-paths ["src/cljs" "src/cljc"]
                      :compiler {:output-to "resources/public/cljs/clojuredocs.js"
                                 :optimizations :advanced
                                 :main "clojuredocs.main"
                                 :pretty-print false
                                 :preamble ["public/js/morpheus.min.js"
                                            "public/js/marked.min.js"
                                            "public/js/fastclick.min.js"]
                                 :externs ["externs/morpheus.js"
                                           "externs/marked.js"
                                           "externs/fastclick.js"]}
                      :jar true}}}
  :figwheel {:http-server-root "resources/public"
             :css-dirs ["resources/public/css"]
             :repl false})
