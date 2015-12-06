(ns twitter-hashtags.core
  "Functions for generation of user timeline status updates and
  user timeline hashtags usage report."
  (:use twitter.api.restful twitter.api.streaming)
  (:require
    twitter.callbacks.protocols
    [clojure.core.async :refer [<!! >!! chan]]
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
  The twitter streaming API streams unknown chars besides '\r\n' to keep the
  connection alive, which can't be parsed by JSON libs. We just need the tweet
  text, so using regex instead."
  [tweet-response]
  (second
    (re-find (re-pattern "\"text\":\"(.*)\"[,}]")
             tweet-response)))

(defn tweet? [s]
  (boolean
    (re-find (re-pattern "\"text\":\"") s)))

(defn decorate-report [report]
  (str (format-local-time (local-now) :date-hour-minute-second)
       " - User timeline usage for "
       (apply + (vals report))
       " hashtags:\n"
       report "\n"))

(defn update-report [report hashtags]
  (merge-with + report (frequencies hashtags)))

(defn user-tl-tweet-count
  "Gets the number of tweets in the user timeline."
  []
  (let [user-info (users-show :params {:screen_name (env :twitter-handle)}
                              :oauth-creds my-twitter-creds)]
    (-> user-info :body :statuses_count)))

(def tweets-by-page 200)

(defn max-user-tl-tweet-pages
  "Gets the maximum number of ':page's to later get all user timeline tweets."
  [] (-> (user-tl-tweet-count) (/ tweets-by-page) Math/ceil int))

(defn tweets-count-for-page
  "Tweets to get from the users timeline for input 'page'."
  [page]
  (let [rest-tweets-cnt (- (user-tl-tweet-count) (* page tweets-by-page))]
    (if (< rest-tweets-cnt tweets-by-page)
      rest-tweets-cnt
      tweets-by-page)))

(defn distil-tweets
  "Transforms tweets into sequences of hashtags."
  [tweets streamed-tweet-hashes]
  (reduce
    (fn [[hashtags streamed-tweet-hashes :as acc] tweet]
      (let [tweet-hash (hash tweet)]
       (if (streamed-tweet-hashes tweet-hash)
         acc
         [(concat hashtags (tweet->hashtags tweet))
          (conj streamed-tweet-hashes tweet-hash)])))
    [[] streamed-tweet-hashes]
    tweets))

(defn user-tl-page-hashtags
  "Bootstrap the report for hashtag usages from a page of the user timeline."
  [page streamed-tweets-chan]
  (let [statuses (:body (statuses-user-timeline
                          :oauth-creds my-twitter-creds
                          :params {:count (tweets-count-for-page page)
                                   :page page}))
        tweets (map :text statuses)]
    (distil-tweets tweets streamed-tweets-chan)))

(defn bootstrap-report
  "Bootstrap the report for hashtag usages from the user timeline."
  []
  (reduce (fn [[report streamed-tweets-chan] page]
            (let [[hashtags hashes] (user-tl-page-hashtags page streamed-tweets-chan)]
              [(update-report report hashtags) hashes]))
          [{} #{}]
          (range (max-user-tl-tweet-pages))))

(defn user-stream-callback ; dynamic var didn't make any sense here after all
  "Returns a callback that will channel its data back to caller."
  [ch]
  (AsyncStreamingCallback. #(>!! ch (str %2))
                           #(error %)
                           #(fatal %)))

(defn -main []
  (try

    (config-timbre!)
    (println)
    (info "Bootstrapping report..")

    (let [ch (chan)
          streamed-tweets-chan (chan)
          ; asynchronously starts to get tweets that be tweeted
          ; before or ater the report is bootstrapped.
          ; only starts showing them on screen after the report is bootstrapped
          response (user-stream :oauth-creds my-twitter-creds
                                :callbacks (user-stream-callback ch))
          [bootstrapped-report streamed-tweet-hashes] (bootstrap-report)]
      (println "streamed-tweet-hashes" streamed-tweet-hashes)
      (info "Report bootstrapped.")
      (-> bootstrapped-report decorate-report println)
      (info "Starting to report hashtag usage updates from user timeline in real time..\n")
      (loop [report bootstrapped-report]
        (recur
          (or (let [input (<!! ch)]
                (if (tweet? input)
                  (when-let [hashtags (-> input tweet-response->tweet-text tweet->hashtags)]
                    (-> report decorate-report println)
                    (->> hashtags (update-report report)))))
              report))))

    (catch Exception e
      (timbre/report (.getMessage e)))))
