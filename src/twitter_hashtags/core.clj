(ns twitter-hashtags.core
  "Functions for generation of user timeline status updates and
  user timeline hashtags usage report."
  (:use twitter.api.restful twitter.api.streaming)
  (:require
    twitter.callbacks.protocols
    [clj-time.local :refer [local-now format-local-time]]
    [environ.core :refer [env]]
    [clojure.string :as str]
    [taoensso.timbre :as timbre :refer [info error fatal]]
    [twitter.oauth :refer [make-oauth-creds]]
    [twitter-hashtags.logging :refer [config-timbre!]]
    [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet tweet->hashtags]])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(def my-twitter-creds (make-oauth-creds (env :oauth-consumer-key)
                                        (env :oauth-consumer-secret)
                                        (env :oauth-app-key)
                                        (env :oauth-app-secret)))

(def report (atom {}))

(defn rand-status-update []
  (statuses-update :oauth-creds my-twitter-creds
                   :params {:status (hashtagify-tweet (big-lorem-tweet))}))

(defn rand-status-updates
  "Generates a bulk 'amount' or 10 status updates with an interval of 1 second."
  [& [amount]]
  (repeatedly
    (or amount 10)
    (fn [] (rand-status-update)
           (Thread/sleep 1000))))

(defn tweet-response->tweet-text
  "Extracts the tweet text from the tweet response of the streaming API.
  The twitter streaming API streams too much garbage which can't be parsed by
  JSON libs. We just need the tweet text, so using regex instead."
  [tweet-response]
  (-> (re-find (re-pattern "\"text\":\"(.*)\"[,}]")
               tweet-response)
    second))

(defn tweet? [s]
  (boolean
    (re-find (re-pattern "\"text\":\"") s)))

(defn decorate-report [report]
  (str (format-local-time (local-now) :date-hour-minute-second)
       " - User timeline hashtag usage:\n"
       report
       "\n"))

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
      #(error %)
      #(fatal %)))

(defn -main []
  (try
    (config-timbre!)
    (println)
    (info "Bootstrapping report..")
    (-> (bootstrap-report) decorate-report println)
    (info "Report bootstrapped.\n")
    (info "Starting to report hashtag usage of user timeline in real time..\n")
    (user-stream :oauth-creds my-twitter-creds
                 :callbacks *user-stream-callbacks*)
    (loop []
      (Thread/sleep 60000)
      (recur))
  (catch Exception e
      (timbre/report (.getMessage e)))))
