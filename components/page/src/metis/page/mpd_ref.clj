(ns metis.page.mpd-ref
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides a special io page for the standard mpd."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))

(def m {:mp-id "mpd-ref",
        :struct :cont,
        :value ["A"],
        :title "container with single task",
        :descr "Container with one task only",
        :exchpath "A"})

(defn content [conf {:keys [active data]}]
  [:div.uk-container.uk-container-large.uk-padding-large
   (u/input m :Value 100)])
