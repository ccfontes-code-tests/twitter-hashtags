(ns twitter-hashtags.routes
  (:require [twitter-hashtags.hashtags :refer [hashtag-frequencies]]
  	        [twitter-hashtags.views :as views]
            [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]))

(defroutes routes
  (route/resources "/")
  (GET "/" [] (views/main))
  ;(GET "/v1/account/:twitter_account" [twitter_account]
  ;  (hashtag-frequencies twitter_account))
;  (GET "/v1/something [:as {params :params}]
;  	                              (models/something params))
  (ANY "/*" _ (route/not-found "Page not found")))