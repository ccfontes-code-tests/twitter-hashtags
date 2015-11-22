(ns twitter-hashtags.views
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn main []
  ; TODO idempotent so no need to call
  (html5
    [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:title "Twitter hashtags"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      (include-css "/css/compiled/styles.css")]

    [:body
      [:div {:id "app"}
        [:h2 "Figwheel template"]
        [:p "Checkout your developer console."]]
      (include-js "/js/compiled/main.js")]))