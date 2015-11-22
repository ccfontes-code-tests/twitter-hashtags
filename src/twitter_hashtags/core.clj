(ns twitter-hashtags.core
  (:require [twitter-hashtags.routes :refer [routes]]
            [twitter-hashtags.db :refer [def-app-db]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [environ.core :refer [env]]
            [taoensso.timbre :refer [info]]))

(defonce system (atom nil))

(def app-handler (-> routes wrap-json-response wrap-params))

(defn start-web-server [port]
  (let [port (or port (env :port) 3000)]
  	(info "App server started on port" port)
    (run-jetty app-handler {:port port :join? false})))

(defn -main [& [port]]
  (info "Allocating system wide resources..")
  (reset! system
          {:db (def-app-db), :web-server (start-web-server port)}))