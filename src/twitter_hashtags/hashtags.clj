(ns twitter-hashtags.hashtags 
  (:use twitter.api.restful twitter.api.streaming)
  (:require twitter.callbacks.protocols
            [clj-time.local :refer [local-now format-local-time]]
  	        [environ.core :refer [env]]
            [clojure.string :as str]
            [taoensso.timbre :refer [info]]
  	        [twitter.oauth :refer [make-oauth-creds]]
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
  JSON libs. We just need the tweet text, so using regex instead."
  [tweet-response]
  (-> (re-find (re-pattern "\"text\":\"(.*)\"[,}]")
               tweet-response)
    second))

  ;(-> (str/replace tweet-response (re-pattern "^\\{|}$") "")
  ;  (str/split (re-pattern ","))
  ;  (->> (map #(str/split % #":" 2))
  ;    (filter #(= (count %) 2))
  ;    (into {}))
  ;  (get "\"text\"")
  ;  (str/replace (re-pattern "^\"|\"$") ""))

(defn tweet? [s]
  (boolean
    (re-find (re-pattern "\"text\":\"") s)))

(defn decorate-report [report]
  (str "\n"
       (format-local-time (local-now) :date-hour-minute-second)
       " - User timeline hashtag usage:\n"
       report))

(defn update-report [hashtags]
   (swap! report #(merge-with + % (frequencies hashtags))))

(defn bootstrap-report []
  (let [statuses (->> (statuses-user-timeline :oauth-creds my-twitter-creds
                                              :params {:count 200})
                   :body)]
    (doseq [status statuses]
      (-> status :text tweet->hashtags update-report))
    @report))

(def ^:dynamic *user-stream-callbacks*
    (AsyncStreamingCallback.
      #(let [input (str %2)]
        (if (tweet? input)
          (if-let [hashtags (-> input tweet-response->tweet-text tweet->hashtags)]
             (-> hashtags update-report decorate-report println))))
      println
      println))

(defn report-user-hashtag-usage []
  (info "Bootstrapping report..")
  (info "Report bootstrapped.")
  (-> (bootstrap-report) decorate-report println)
  (info "Starting to report hashtag usage of user timeline..")
  (user-stream :oauth-creds my-twitter-creds
               :callbacks *user-stream-callbacks*)
  (loop []
    (Thread/sleep 60000)
    (recur)))
