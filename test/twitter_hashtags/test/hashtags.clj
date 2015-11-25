(ns twitter-hashtags.test.hashtags
  (:use twitter.api.streaming twitter.api.restful)
  (:require [midje.sweet :refer [fact against-background before contains]]
  	        [twitter.oauth :refer [make-oauth-creds]]
  	        [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet]]
            [twitter-hashtags.hashtags :refer [my-twitter-creds]]))

(fact "about twitter status update"
  (-> (statuses-update :oauth-creds my-twitter-creds
                       :params {:status (hashtagify-tweet (big-lorem-tweet))})
    :status)
    => (contains {:code 200}))

;
;(against-background [(before :facts (connect!))]
;  (facts "about twitter user event"
;    ))