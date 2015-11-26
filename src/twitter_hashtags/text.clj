(ns twitter-hashtags.text
  (:require [clojure.string :as str])
  (:import [com.twitter Extractor]
  	       [net._01001111.text LoremIpsum]))

(def tweet-max-chars 140)

(defn valid-tweet-size? [tweet]
  (<= (count tweet) tweet-max-chars))

(defn rand-idx [coll]
  (int (* (count coll) (rand))))

(def max-hashtags 3)

(defn amount-hashtags []
  (-> (rand) (* max-hashtags) Math/round))

(defn hashtagify-word [word]
  (if (= (first word) \#)
    word
    (str "#" word)))

(defn tweet->hashtags
  "Extracts hashtags from tweet."
  [tweet]
  (seq (.extractHashtags (Extractor.) tweet)))

(defn big-lorem-tweet []
  "Creates a random lorem ipsum twitter sized message."
  (reduce (fn [lorem ipsum]
            (let [lorem-ipsum (str lorem (some->> ipsum (str " ")))]
              (if (valid-tweet-size? lorem-ipsum)
                lorem-ipsum
                (reduced lorem))))
          (repeatedly #(.randomWord (LoremIpsum.)))))

(defn hashtagify-tweet
  "Hashtags up to max-hashtags from words of input tweet."
  [tweet]
  (let [n-hashtags (amount-hashtags)
        avail-tweet-chars (- tweet-max-chars (count tweet))
        n-repeat (if (< avail-tweet-chars n-hashtags)
                   avail-tweet-chars
                   n-hashtags)
  	    tweet (str/split tweet #" ")]
    (->> (reduce (fn [tweet idx]
    	           (update tweet idx hashtagify-word))
                 tweet
                 (repeatedly n-repeat #(rand-idx tweet)))
      (str/join " "))))