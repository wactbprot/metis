(ns metis.page.mpd-ppc-gas_dosing
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides a special io page for mpd-ppc-gas_dosing."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))


(defn channel-value-input [ch label]
  [:div
   [:label.uk-form-label
    {:for (str "mpd-ppc-gas_dosing_exch_" ch "_Value")}
    label]
    [:div.uk-form-controls
     [:input.uk-input.exch-input
      {:type "text"
       :id (str "mpd-ppc-gas_dosing_exch_" ch "_Value")}]]])

(defn content [conf {:keys [active data]}]
  [:div.uk-container.uk-container-large.uk-padding-large
   [:div {:uk-grid ""}
    ;; pressure select
    [:div.uk-card.uk-card-body.uk-card-default
     [:h3.uk-card-title "Target Pressure"]
     [:div.uk-form-controls
      (into [:select.uk-select.exch-select
             {:id "id"
              :data-mp-id "mpd-ppc-gas_dosing"
              :data-struct "exch"
              :data-exchpath "TargetPressure"
              :data-type "float"}
             [:option {:value "-"} "-"]]
            (mapv (fn [{:keys [value display]}]
                    [:option {:value value} (or display value)])
                  [{:value 1 :display "1mbar"}]))]]
    [:div.uk-card.uk-card-body.uk-card-default
     [:h3.uk-card-title "Target Reservoir"]
     [:div.uk-form-controls
      (into [:select.uk-select.exch-select
             {:id "id"
              :data-mp-id "mpd-ppc-gas_dosing"
              :data-struct "exch"
              :data-exchpath "TargetReservoir"
              :data-type "string"}
             [:option {:value "-"} "-"]]
            (mapv (fn [{:keys [value display]}]
                    [:option {:value value} (or display value)])
                  [{:value 3 :display "Reservoir 3"}
                   {:value 4 :display "Reservoir 4"}
                   {:value 5 :display "Reservoir 5"}]))]]
    ;; pressure indication
   [:div {:uk-grid ""}
    [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
     [:h3.uk-card-title "Reservoir 3"]
     (channel-value-input "CH1" "1000T CDG (Ch1, mbar)")
     (channel-value-input "CH2" "10T CDG (Ch2, mbar)")]
    [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
     [:h3.uk-card-title "Reservoir 4"]
     (channel-value-input "CH3" "10T CDG (Ch3, mbar)")
     (channel-value-input "CH4" "0.1T CDG (Ch4, mbar)")]
    [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
     [:h3.uk-card-title "Reservoir 5"]
     (channel-value-input "CH5" "10T CDG (Ch5, mbar)")
     (channel-value-input "CH6" "0.1T CDG (Ch6, mbar)")]]]])
