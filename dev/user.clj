(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require [twitter-hashtags.core :refer [system]]
            [twitter-hashtags.db :refer [def-app-db]]
            [twitter-hashtags.core :refer [system]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  [] (-> @system :web-server .stop)
     (-> @system :db :pool deref :datasource .close))

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  [] (stop)
     (refresh :after `twitter-hashtags.core/-main))
