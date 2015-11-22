(defproject twitter-hashtags "0.1.0-SNAPSHOT"

  :description "Frequency of hashtag usages on a twitter account."

  :url "http://hashtags.gq"

  :scm {:name "git", :url "https://github.com/ccfontes/twitter-hashtags"}

  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}

  :main twitter-hashtags.core

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/css/compiled"
                                    "target"]

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
               ;; :server-port 3449 ;; default
               ;; :server-ip "127.0.0.1"
  
               :css-dirs ["resources/public/css"] ;; watch and update CSS
  
               ;; Start an nREPL server into the running figwheel process
               ;; :nrepl-port 7888
  
               ;; Server Ring Handler (optional)
               ;; if you want to embed a ring handler into the figwheel http-kit
               ;; server, this is for simple ring servers, if this
               ;; doesn't work for you just run your own server :)
               :ring-handler twitter-hashtags.core/app-handler
  
               ;; To be able to open files in your editor from the heads up display
               ;; you will need to put a script on your path.
               ;; that script will have to take a file path and a line number
               ;; ie. in  ~/bin/myfile-opener
               ;; #! /bin/sh
               ;; emacsclient -n +$2 $1
               ;;
               ;; :open-file-command "myfile-opener"
  
               ;; if you want to disable the REPL
               ;; :repl false
  
               ;; to configure a different figwheel logfile path
               ;; :server-logfile "tmp/logs/figwheel-logfile.log"
               }

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src-cljs"]

                :figwheel {:on-jsload "twitter-hashtags.core/on-js-reload"}

                :compiler {:main twitter-hashtags.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/main.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"

                :source-paths ["src-cljs"]
                
                :compiler {:output-to "resources/public/js/compiled/main.js"
                           :main twitter-hashtags.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :garden {:builds [{:source-paths ["dev"]
                     :stylesheet styles/styles
                     :compiler {:output-to "resources/public/css/compiled/styles.css"}}]}

  :aliases {"dist" ["do" ["garden" "once"] ["cljsbuild" "once"]]}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [hiccup "1.0.5"]
                 [sablono "0.4.0"]
                 [org.omcljs/om "0.9.0"]
                 [prismatic/om-tools "0.4.0"]
                 ; in compojure exclusion: midje needs more recent commons-codec
                 [compojure "1.4.0" :exclusions [commons-codec]]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [korma "0.4.2"]
                 [twitter-api "0.7.8"]
                 [ring/ring-json "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]
                 [com.taoensso/timbre "4.1.4"]
                 [environ "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]
            [lein-garden "0.2.6"]]

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [midje "1.8.2"]
                                  [garden "1.3.0-SNAPSHOT"]
                                  [ring/ring-mock "0.3.0"]
                                  [cheshire "5.5.0"]]
                   :source-paths ["dev"]}}

  :repl-options
      {:init (do (require '[twitter-hashtags.core :as core]) (core/-main)
                 (use 'midje.repl) (autotest))
       :init-ns user
       :welcome
         (println "Type (reset) to reload all namespaces and reinstate the system wide resources.")})
