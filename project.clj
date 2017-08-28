(defproject commiteth "0.1.0-SNAPSHOT"
  :description "Ethereum bounty bot for Github"
  :url "https://commiteth.com"
  :dependencies [[metosin/compojure-api "1.1.11"
                  :exclusions [joda-time]]
                 [re-frame "0.10.1"]
                 [cljs-ajax "0.6.0"]
                 [secretary "1.2.3"]
                 [reagent-utils "0.2.1"]
                 [reagent "0.7.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/clojure "1.8.0"]
                 [selmer "1.11.0"]
                 [markdown-clj "0.9.99"]
                 [ring-middleware-format "0.7.2"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-codec "1.0.1"]
                 [metosin/ring-http-response "0.9.0"]
                 [bouncer "1.0.1"
                  :exclusions [joda-time]]
                 [org.clojure/tools.logging "0.4.0"]
                 [compojure "1.6.0"]
                 [http-kit "2.2.0"]
                 [mount "0.1.11"]
                 [cprop "0.1.11"]
                 [org.clojure/tools.cli "0.3.5"]
                 [luminus-nrepl "0.1.4"]
                 [buddy/buddy-auth "2.0.0"]
                 [luminus-migrations "0.4.0"]
                 [conman "0.6.7"]
                 [org.postgresql/postgresql "42.1.4"]
                 [luminus-immutant "0.2.3"]
                 [clj.qrgen "0.4.0"]
                 [digest "1.4.5"]
                 [tentacles "0.5.1"]
                 [re-frisk "0.4.5"]
                 [bk/ring-gzip "0.2.1"]
                 [crypto-random "1.2.0"]
                 [crypto-equality "1.0.0"]
                 [cheshire "5.8.0"]
                 [mpg "1.3.0"]
                 [pandect "0.6.1"]
                 [cljsjs/moment "2.17.1-1"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [com.cemerick/piggieback "0.2.2"]
                 [jarohen/chime "0.2.2"]
                 [com.andrewmcveigh/cljs-time "0.5.1"]
                 [akiroz.re-frame/storage "0.1.2"]
                 [cljsjs/chartjs "2.6.0-0"]
                 [org.web3j/core "2.3.0"]
                 [cljs-web3 "0.19.0-0-2"]]

  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]
  :java-source-paths ["src/java"]
  :target-path "target/%s/"
  :repositories {"MVN repository" "https://mvnrepository.com"}
  :main ^:skip-aot commiteth.core
  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.4.1"]
            [lein-cljsbuild "1.1.7"]
            [lein-auto "0.1.2"]
            [lein-less "1.7.5"]
            [lein-shell "0.5.0"]
            [cider/cider-nrepl "0.14.0"]
            [lein-sha-version "0.1.1"]]


  :less {:source-paths ["src/less"]
         :target-path "resources/public/css"}

  :auto {"build-contracts" {:file-pattern #"\.(sol)\n"
                            :paths ["./contracts"]}}

  :aliases {"build-contracts" ["shell" "./build_contracts.sh"]}

  :ring {:destroy commiteth.scheduler/stop-scheduler}

  :uberjar-exclusions [#"public/README.md" #"public/cards.html"]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port       7002
   :css-dirs         ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar       {:omit-source    true
                   :prep-tasks     ["build-contracts" "javac" "compile" ["cljsbuild" "once" "min"] ["less" "once"]]
                   :cljsbuild
                   {:builds
                    {:min
                     {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                      :jar true
                      :compiler
                      {:output-to     "target/cljsbuild/public/js/app.js"
                       :externs       ["externs/web3-externs.js"]
                       :optimizations :advanced
                       :pretty-print  false
                       :closure-defines {goog.DEBUG false}
                       :closure-warnings
                       {:externs-validation :off
                        :non-standard-jsdoc :off}}}}}


                   :aot            :all
                   :uberjar-name   "commiteth.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}


   :dev   {:dependencies   [[prone "1.1.4"]
                            [ring/ring-mock "0.3.1"]
                            [ring/ring-devel "1.6.2"]
                            [pjstadig/humane-test-output "0.8.2"]
                            [doo "0.1.7"]
                            [binaryage/devtools "0.9.4"]
                            [figwheel-sidecar "0.5.13"]
                            [org.clojure/tools.nrepl "0.2.13"]
                            [com.cemerick/piggieback "0.2.2"]
                            [sablono "0.8.0"]]
           :plugins        [[com.jakemccrary/lein-test-refresh "0.14.0"]
                            [lein-doo "0.1.7"]
                            [lein-figwheel "0.5.10"]]
           :cljsbuild
           {:builds
            [{:id "app"
              :source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
              :compiler
              {:main          "commiteth.app"
               :asset-path    "/js/out"
               :output-to     "target/cljsbuild/public/js/app.js"
               :output-dir    "target/cljsbuild/public/js/out"
               :source-map    true
               :optimizations :none
               :pretty-print  true}}]}

           :doo            {:build "test"}
           :source-paths   ["env/dev/clj" "test/clj"]
           :resource-paths ["env/dev/resources"]
           :repl-options   {:init-ns user
                            :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
           :injections     [(require 'pjstadig.humane-test-output)
                            (pjstadig.humane-test-output/activate!)]}
   :test  {:resource-paths ["env/dev/resources" "env/test/resources"]
           :dependencies   [[devcards "0.2.3"]]
           :cljsbuild
           {:builds
            {:test
             {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
              :compiler
              {:output-to     "target/test.js"
               :main          "commiteth.doo-runner"
               :optimizations :whitespace
               :pretty-print  true}}
             :devcards {:source-paths ["src/cljs" "src/cljc"]
                        :figwheel {:devcards true}
                        :compiler {:main commiteth.cards
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/devcards.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}}}}})
