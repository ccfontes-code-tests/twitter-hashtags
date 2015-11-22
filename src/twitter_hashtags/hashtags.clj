(ns twitter-hashtags.hashtags
  (:require [twitter.oauth :refer [make-oauth-creds]]))

(defn hashtag-frequencies [twitter-account]
  {:body {:frequencies ""}})