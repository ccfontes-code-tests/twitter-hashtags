(ns twitter-hashtags.hashtags 
  (:use twitter.api.restful)
  (:require twitter.callbacks.protocols
  	        [environ.core :refer [env]]
  	        [clojure.data.json :refer [read-json]]
  	        ;[twitter.callbacks.handlers :refer [response-return-everything]]
  	        [twitter.oauth :refer [make-oauth-creds]]
  	        [twitter.api.streaming :refer :all]
  	        [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet]])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(def my-twitter-creds (make-oauth-creds (env :oauth-consumer-key)
                                        (env :oauth-consumer-secret)
                                        (env :oauth-app-key)
                                        (env :oauth-app-secret)))

(defn rand-status-update []
  (statuses-update :oauth-creds my-twitter-creds
                   :params {:status (hashtagify-tweet (big-lorem-tweet))}))

(defn gen-status-update
  "Generates a bulk 'amount' or 10 status updates with an interval of 1 second."
  [& [amount]]
  (repeatedly
  	(or amount 10)
    (fn [] (rand-status-update)
           (Thread/sleep 1000))))

(defn hashtag-frequencies [twitter-account]
  {:body {:frequencies ""}})

(defn tweet->hashtags [tweet]
  )

(def ^:dynamic *user-stream-callbacks*
  (AsyncStreamingCallback. (comp println #(:text %) read-json #(str %2))
  	                       println
  	                       println))

(defn report-user-hashtag-usage [& {:keys [oauth-creds]}]
  (user-stream :oauth-creds my-twitter-creds
  	           :callbacks *user-stream-callbacks*)
  (loop []
    (Thread/sleep 2000)
    (recur)))