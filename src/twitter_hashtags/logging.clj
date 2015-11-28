(ns twitter-hashtags.logging
  "Logging configuration"
  (:require [taoensso.timbre :as timbre :refer [default-output-fn color-str]]
            [taoensso.encore :refer [spaced-str]]))

(defn config-timbre! []
  (let [colors {:info :green, :warn :yellow, :error :red, :fatal :purple, :report :blue}]
    (timbre/set-config!
      {:level :debug
       :output-fn default-output-fn
       :appenders
       {:color-appender
         {:enabled?   true
          :async?     false
          :min-level  nil
          :rate-limit nil
          :output-fn  :inherit
          :fn (fn [{:keys [error? level output-fn] :as data}]
                (binding [*out* (if error? *err* *out*)]
                  (if-let [color (colors level)]
                    (println (color-str color (output-fn data)))
                    (println (output-fn data)))))}}})))