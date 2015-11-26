(ns twitter-hashtags.test.text
  (:require [midje.sweet :refer [fact facts]]
            [twitter-hashtags.text :refer :all]
            [twitter-hashtags.test.fixtures :refer [lorem-ipsum-tweet]]
            [clojure.string :as str]))

(facts "about 'valid-tweet-size?'"
  (-> (repeat 140 "x") str/join valid-tweet-size?) => true
  (-> (repeat 141 "x") str/join valid-tweet-size?) => false)

(facts "about 'rand-idx'"
  (let [coll [3 8 1]]
    (rand-idx coll) => integer?
    (rand-idx coll) => #(<= 0 % (- (count coll) 1))))

(facts "about 'amount-hashtags'"
  (amount-hashtags) => #(<= 0 % max-hashtags))

(facts "about 'hashtagify-word'"
  (hashtagify-word "sermo") => "#sermo")

(fact "about 'tweet->hashtags'"
  (tweet->hashtags lorem-ipsum-tweet) => ["egestas" "eros" "est"]
  (tweet->hashtags nil)               => nil
  (tweet->hashtags "")                => nil)

(facts "about 'big-lorem-tweet'"
  (big-lorem-tweet)          => string?
  (big-lorem-tweet)          => valid-tweet-size?)

(facts "about 'hashtagify-tweet'"
  (hashtagify-tweet (big-lorem-tweet)) => string?
  (hashtagify-tweet (big-lorem-tweet)) => valid-tweet-size?)
