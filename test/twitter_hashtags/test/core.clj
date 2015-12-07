(ns twitter-hashtags.test.core
  (:use midje.sweet twitter-hashtags.core twitter-hashtags.text)
  (:require [twitter-hashtags.test.fixtures :refer [response-tweet response-friends]]))

(fact "about twitter status update"
  (-> (rand-status-update) :status) => (contains {:code tweets-by-page}))

(fact "about 'tweet-response->text-text'"
  (tweet-response->tweet-text response-tweet) => "lro\"te{\"\"}uoretr lt #bar")

(facts "about 'tweet?'"
  (tweet? response-tweet)   => true
  (tweet? response-friends) => false)

(fact "about 'update-report'"
  (let [report {"sermo" 1 "netus" 2}
        hashtags ["sermo" "sermo" "gravida"]]
    (update-report report hashtags) => (contains {"sermo" 3 "gravida" 1})))

(fact "about 'distil-tweets'"
  (let [repeated-tweet "#sermo netus"
        original-tweet "#netus gravida"
        hash-repeated-tweets #{(hash repeated-tweet)}]
  (distil-tweets [original-tweet repeated-tweet] hash-repeated-tweets)
    => [(tweet->hashtags original-tweet)
        (conj hash-repeated-tweets (hash original-tweet))]))

(facts "about 'user-tl-page-hashtags'"
  (let [[report streamed-tweet-hashes] (user-tl-page-hashtags 0 #{})]
    report                => (every-pred seq (comp string? first))
    streamed-tweet-hashes => (comp integer? first)))