(ns twitter-hashtags.hashtags 
  (:use twitter.api.restful)
  (:require twitter.callbacks.protocols
            [clj-time.local :refer [local-now format-local-time]]
  	        [environ.core :refer [env]]
            [clojure.string :as str]
            [clojure.data.json :refer [read-json]]
  	        [twitter.oauth :refer [make-oauth-creds]]
  	        [twitter.api.streaming :refer :all]
  	        [twitter-hashtags.text
              :refer [big-lorem-tweet hashtagify-tweet tweet->hashtags]])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(def my-twitter-creds (make-oauth-creds (env :oauth-consumer-key)
                                        (env :oauth-consumer-secret)
                                        (env :oauth-app-key)
                                        (env :oauth-app-secret)))

(def report (atom {}))

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

(defn tweet-response->tweet-text
  "Extracts the tweet text from the tweet response of the streaming API.
  The twitter streaming API streams too much garbage which can't be parsed by
  JSON libs. We just need the tweet text, so using splits and regexps instead."
  [tweet-response]
  (-> (str/replace tweet-response (re-pattern "^\\{|}$") "")
    (str/split (re-pattern ","))
    (->> (map #(str/split % #":" 2))
      (filter #(= (count %) 2))
      (into {}))
    (get "\"text\"")
    (str/replace (re-pattern "^\"|\"$") "")))

(defn tweet? [s]
  (boolean
    (re-find (re-pattern "\"text\":\"") s)))

(defn decorate-report [report]
  (str
    (format-local-time (local-now) :date-hour-minute-second)
    " - Hashtag usage: "
    report))

(defn update-report [new-usage]
  (swap! report #(merge-with + % new-usage)))

(def ^:dynamic *user-stream-callbacks*
    (AsyncStreamingCallback.
      #(let [stream-input (str %2)]
        (if (tweet? stream-input)
          (if-let [hashtags (-> stream-input tweet-response->tweet-text tweet->hashtags)]
             (-> hashtags frequencies update-report decorate-report println))))
      println
      println))

(defn report-user-hashtag-usage []
  (user-stream :oauth-creds my-twitter-creds
  	           :callbacks *user-stream-callbacks*)
  (loop []
    (Thread/sleep 60000)
    (recur)))
