(ns twitter-hashtags.hashtags
  (:require [environ.core :refer [env]]
  	        [twitter.oauth :refer [make-oauth-creds]]))

(def my-twitter-creds (make-oauth-creds (env :oauth-consumer-key)
                                        (env :oauth-consumer-secret)
                                        (env :oauth-app-key)
                                        (env :oauth-app-secret)))

(defn hashtag-frequencies [twitter-account]
  {:body {:frequencies ""}})