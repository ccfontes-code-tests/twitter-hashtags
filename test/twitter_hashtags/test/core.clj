(ns twitter-hashtags.test.core
  (:use midje.sweet
  	    twitter.api.streaming
  	    twitter.api.restful
  	    twitter-hashtags.core)
  (:require [twitter.oauth :refer [make-oauth-creds]]
  	        [twitter-hashtags.text :refer [big-lorem-tweet hashtagify-tweet]]
  	        [twitter-hashtags.test.fixtures :refer [response-tweet response-friends]]))

(fact "about twitter status update"
  (-> (statuses-update :oauth-creds my-twitter-creds
                       :params {:status (hashtagify-tweet (big-lorem-tweet))})
    :status)
    => (contains {:code 200}))

(fact "about 'tweet-response->text-text'"
  (tweet-response->tweet-text response-tweet) => "lro\"te{\"\"}uoretr lt #bar")

(facts "about 'tweet?'"
  (tweet? response-tweet) => true
  (tweet? response-friends) => false)