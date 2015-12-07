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
    [taoensso.timbre :as timbre :refer [info fatal]]
    [twitter.oauth :refer [make-oauth-creds]]
    [twitter-hashtags.logging :refer [config-timbre!]]
    [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet tweet->hashtags]])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(def my-twitter-creds (make-oauth-creds (env :oauth-consumer-key)
                                        (env :oauth-consumer-secret)
                                        (env :oauth-app-key)
                                        (env :oauth-app-secret)))

; maximum per distinct request
; source: https://dev.twitter.com/rest/reference/get/statuses/user_timeline
(def tweets-by-page 200)

(defn rand-status-update []
  (statuses-update :oauth-creds my-twitter-creds
                   :params {:status (hashtagify-tweet (big-lorem-tweet))}))

(defn rand-status-updates
  "Generates a bulk 'amount' or 10 status updates with an interval of 1 second."
  [& [amount]]
  (repeatedly
    (or amount 1)
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
                          :params {:count tweets-by-page, :page page}))
        tweets (map :text statuses)]
    (if (seq statuses)
      (distil-tweets tweets streamed-tweets-chan))))

(defn bootstrap-report
  "Bootstrap the report for hashtag usages from the user timeline."
  []
  (reduce (fn [[report streamed-tweets-chan :as acc] page]
            (let [[hashtags hashes :as res] (user-tl-page-hashtags page streamed-tweets-chan)]
              (if res
                [(update-report report hashtags) hashes]
                (reduced acc))))
          [{} #{}]
          (range)))

(defn user-stream-callback ; dynamic var didn't make any sense here after all
  "Returns a callback that will channel its data back to caller."
  [ch]
  (AsyncStreamingCallback. #(>!! ch (str %2))
                           (constantly nil)
                           (constantly nil)))

(defn start-user-tl-stream [ch]
  (user-stream :oauth-creds my-twitter-creds
               :callbacks (user-stream-callback ch)))

(defn restart-user-tl-stream [user-stream ch]
  ((:cancel (meta user-stream)))
  (start-user-tl-stream ch))

(defn- report-from-stream
  "Updates report from real-time status updates,
  and presents the update report on the screen"
  [ch report-bootstrap-res]
  (loop [[report streamed-tweet-hashes :as unchanged] report-bootstrap-res]
    (recur
      (or
        (let [response (<!! ch)]
          (if (tweet? response)
            (let [tweet (tweet-response->tweet-text response)
                  [hashtags hashes] (distil-tweets [tweet] streamed-tweet-hashes)
                  stream-report (update-report report hashtags)]
              (if (seq hashtags)
                (-> stream-report decorate-report println))
              [stream-report hashes])))
        unchanged))))

(defn report-hashtag-usage-realtime-msg [first-word]
  (str first-word
       " to report hashtag usage updates from user timeline in real time..\n"))

(defn -main []
  (try

    (config-timbre!)
    (println)
    (info "Bootstrapping report..")
    (let [ch (chan)
          ; asynchronously starts to get tweets that can be tweeted
          ; before or ater the report is bootstrapped.
          ; only starts showing them on screen after the report is bootstrapped
          user-stream-atom (atom (start-user-tl-stream ch))
          [bootstrapped-report _ :as bootstrap-res] (bootstrap-report)]

      (info "Report bootstrapped.")
      (-> bootstrapped-report decorate-report println)

      (info (report-hashtag-usage-realtime-msg "Starting"))
      (try
        (report-from-stream ch bootstrap-res)
        (catch Exception e
          (fatal (.getMessage e))
          (info (report-hashtag-usage-realtime-msg "Restarting"))
          (reset! user-stream-atom (restart-user-tl-stream @user-stream-atom ch)))))

    (catch Exception e
      (timbre/report (.getMessage e)))))
