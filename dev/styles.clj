(ns styles
  (:require [garden.def :refer [defstyles]]
            ;[garden.units :refer [px em percent]]
            ))

(defstyles styles
  [:body {:background-image "url('../../img/hashtag-bg.jpg')"
          :background-repeat "no-repeat"
          :background-size "cover"}])
