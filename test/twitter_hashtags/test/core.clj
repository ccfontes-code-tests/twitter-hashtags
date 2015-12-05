(ns twitter-hashtags.test.core
  (:use midje.sweet twitter-hashtags.core)
  (:require [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet]]
  	        [twitter-hashtags.test.fixtures :refer [response-tweet response-friends]]
            [twitter-hashtags.test.util :refer [pos-int?]]))

(fact "about twitter status update"
  (-> (rand-status-update) :status) => (contains {:code tweets-by-page}))

(fact "about 'tweet-response->text-text'"
  (tweet-response->tweet-text response-tweet) => "lro\"te{\"\"}uoretr lt #bar")

(facts "about 'tweet?'"
  (tweet? response-tweet)   => true
  (tweet? response-friends) => false)

(facts "about 'user-tl-tweet-count' and 'max-user-tl-tweet-pages'"
  (user-tl-tweet-count)     => pos-int?
  (max-user-tl-tweet-pages) => pos-int?)

(fact "about 'update-report'"
  (let [report-backup @report]
    (reset! report {})
    (update-report ["sermo" "sermo" "gravida"]) => (contains {"sermo" 2 "gravida" 1})
    (reset! report report-backup)))