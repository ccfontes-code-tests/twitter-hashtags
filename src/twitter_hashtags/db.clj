(ns twitter-hashtags.db
  (:require [korma.db :refer [sqlite3 defdb]]))

(def db-map (sqlite3 {:db "db/development.sqlite3"}))

(defn def-app-db [] (defdb db db-map))
