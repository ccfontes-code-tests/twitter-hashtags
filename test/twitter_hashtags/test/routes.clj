(ns twitter-hashtags.test.routes
  (:require [midje.sweet :refer [fact facts]]
            [ring.mock.request :as mock]
            [twitter-hashtags.core :refer [app-handler]]))

(fact "about '/' route"
  (let [{:keys [status headers body]} (app-handler (mock/request :get "/"))]
  	status => 200
  	headers => {"Content-Type" "text/html; charset=utf-8"}
  	body => (every-pred string? seq))) ; TODO use hickory for better test if not using resources from figwheel

(facts "about '/v1/account/:twitter-account' route"
  (let [{:keys [status body]} (app-handler (mock/request :get "/v1/account/ccfontes"))]
  ;  status => 200
  ;  (:frequencies body) => seq
  ))

(fact "about not found route"
  (-> (mock/request :get "/doesnt_exist") app-handler :status) => 404)