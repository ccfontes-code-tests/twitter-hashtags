(defproject twitter-hashtags "0.1.0-SNAPSHOT"

  :description "Frequency of hashtag usages on a twitter account."

  :scm {:name "git", :url "https://github.com/ccfontes/twitter-hashtags"}

  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}

  :main twitter-hashtags.core

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [twitter-api "0.7.8"]
                 [com.taoensso/timbre "4.1.4"]
                 [environ "1.0.1"]
                 [net._01001111/jlorem "1.3"]
                 [com.twitter/twitter-text "1.6.1"]
                 [clj-time "0.11.0"]]
 
  :plugins [[lein-environ "1.0.1"]]

  :profiles {:dev [:project/dev :profiles/dev]
             :project/dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                          [midje "1.8.2"]
                                          [org.clojure/data.json "0.2.5"]]
                           :source-paths ["dev"]}
             :profiles/dev {}} ; Only change this in ./profiles.clj

  :repl-options
      {:init (do (use 'midje.repl) (autotest))
       :init-ns user
       :timeout 50000
       :welcome (println "Type (refresh) to reload all namespaces.")})
