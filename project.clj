(defproject commiteth "0.1.0-SNAPSHOT"

  :description "Ethereum bounty bot for Github"
  :url "https://commiteth.com"

  :dependencies [[metosin/compojure-api "1.1.10"]
                 [re-frame "0.9.1"]
                 [cljs-ajax "0.5.8"]
                 [secretary "1.2.3"]
                 [reagent-utils "0.2.0"]
                 [reagent "0.6.0"]
                 [org.clojure/clojurescript "1.9.456" :scope "provided"]
                 [org.clojure/clojure "1.8.0"]
                 [selmer "1.10.5"]
                 [markdown-clj "0.9.93"]
                 [ring-middleware-format "0.7.2"]
                 [metosin/ring-http-response "0.8.1"]
                 [bouncer "1.0.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.5.2"]
                 [http-kit "2.2.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.2.2"]
                 [ring/ring-codec "1.0.1"]
                 [mount "0.1.11"]
                 [cprop "0.1.10"]
                 [org.clojure/tools.cli "0.3.5"]
                 [luminus-nrepl "0.1.4"]
                 [buddy "1.3.0"]
                 [buddy/buddy-auth "1.4.1"]
                 [luminus-migrations "0.2.9"]
                 [conman "0.6.3"]
                 [org.postgresql/postgresql "9.4.1212"]
                 [luminus-immutant "0.2.3"]
                 [clj.qrgen "0.4.0"]
                 [digest "1.4.5"]
                 [tentacles "0.5.1"]
                 [re-frisk "0.3.2"]
                 [bk/ring-gzip "0.2.1"]
                 [crypto-random "1.2.0"]
                 [crypto-equality "1.0.0"]
                 [cheshire "5.7.0"]]

  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main commiteth.core
  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.4.1"]
            [lein-cljsbuild "1.1.3"]
            [lein-auto "0.1.2"]
            [lein-less "1.7.5"]]

  :user {:plugins  [[cider/cider-nrepl "0.15.0-SNAPSHOT"]]}

  :less {:source-paths ["src/less"]
         :target-path "resources/public/css"}

  :ring {:destroy commiteth.scheduler/stop-scheduler}


  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port       7002
   :css-dirs         ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar       {:omit-source    true
                   :prep-tasks     ["compile" ["cljsbuild" "once" "min"]]
                   :cljsbuild
                   {:builds
                    {:min
                     {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                      :compiler
                      {:output-to     "target/cljsbuild/public/js/app.js"
                       :externs       ["react/externs/react.js"]
                       :optimizations :advanced
                       :pretty-print  false
                       :closure-warnings
                       {:externs-validation :off :non-standard-jsdoc :off}}}}}


                   :aot            :all
                   :uberjar-name   "commiteth.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}


   :dev   {:dependencies   [[prone "1.1.4"]
                            [ring/ring-mock "0.3.0"]
                            [ring/ring-devel "1.5.1"]
                            [pjstadig/humane-test-output "0.8.1"]
                            [doo "0.1.7"]
                            [binaryage/devtools "0.9.0"]
                            [figwheel-sidecar "0.5.9"]
                            [org.clojure/tools.nrepl "0.2.12"]
                            [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                            [sablono "0.7.7"]]
           :plugins        [[com.jakemccrary/lein-test-refresh "0.14.0"]
                            [lein-doo "0.1.7"]
                            [lein-figwheel "0.5.9"]]
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
           :repl-options   {:init-ns user}
           :injections     [(require 'pjstadig.humane-test-output)
                            (pjstadig.humane-test-output/activate!)]}
   :test  {:resource-paths ["env/dev/resources" "env/test/resources"]
           :dependencies   [[devcards "0.2.2"]]
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
