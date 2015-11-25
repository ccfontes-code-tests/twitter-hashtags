(ns twitter-hashtags.test.text
  (:require [midje.sweet :refer [facts]]
            [twitter-hashtags.text :refer :all]
            [clojure.string :as str]))

(facts "about 'valid-tweet-size?'"
  (-> (repeat 140 "x") str/join valid-tweet-size?) => true
  (-> (repeat 141 "x") str/join valid-tweet-size?) => false)

(facts
  (let [coll [3 8 1]]
    (rand-idx coll) => integer?
    (rand-idx coll) => #(<= 0 % (- (count coll) 1))))

(facts
  (amount-hashtags) => #(<= 0 % max-hashtags))

(facts
  (hashtagify-word "sermo") => "#sermo")

(facts "about 'big-lorem-tweet'"
  (big-lorem-tweet)          => string?
  (big-lorem-tweet)          => valid-tweet-size?)

(facts "about 'hashtagify-tweet'"
  (hashtagify-tweet (big-lorem-tweet)) => string?
  (hashtagify-tweet (big-lorem-tweet)) => valid-tweet-size?)