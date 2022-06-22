(ns metis.page.mpd-ppc-gas_dosing
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides a special io page for mpd-ppc-gas_dosing."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))

(def m {:mp-id "mpd-ppc-gas_dosing",
        :struct :cont,
        :exchpath "CH1"})

(defn content [conf {:keys [active data]}]
  [:div.uk-container.uk-container-large.uk-padding-large
   (u/input m :Value nil)])
