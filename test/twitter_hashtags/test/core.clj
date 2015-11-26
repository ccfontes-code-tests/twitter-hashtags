(ns twitter-hashtags.test.core
  (:use midje.sweet twitter-hashtags.core)
  (:require [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet]]
  	        [twitter-hashtags.test.fixtures :refer [response-tweet response-friends]]))

(fact "about twitter status update"
  (-> (rand-status-update) :status) => (contains {:code 200}))

(fact "about 'tweet-response->text-text'"
  (tweet-response->tweet-text response-tweet) => "lro\"te{\"\"}uoretr lt #bar")

(facts "about 'tweet?'"
  (tweet? response-tweet) => true
  (tweet? response-friends) => false)

(fact "about 'update-report'"
  (let [report-backup @report]
    (reset! report {})
    (update-report ["sermo" "sermo" "gravida"])
      => (contains {"sermo" 2 "gravida" 1})
    (reset! report report-backup)))