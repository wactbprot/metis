(ns metis.page.mpd-ppc-gas_dosing
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides a special io page for mpd-ppc-gas_dosing."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))


(def base-map {:data-mp-id "mpd-ppc-gas_dosing"
               :data-struct "exch"})

(def target-pressure-opts 
  [{:value 0.1 :display "0.1 mbar"}
   {:value 0.1 :display "0.2 mbar"}
   {:value 1 :display "1 mbar"}
   {:value 2 :display "2 mbar"}
   {:value 3 :display "3 mbar"}])

(def target-reservoir-opts
  [{:value 3 :display "Reservoir 3"}
   {:value 4 :display "Reservoir 4"}
   {:value 5 :display "Reservoir 5"}])

(defn channel-value-input [ch label]
  [:div
   [:label.uk-form-label
    {:for (str "mpd-ppc-gas_dosing_exch_" ch "_Value")}
    label]
    [:div.uk-form-controls
     [:input.uk-input.exch-input
      {:type "text"
       :id (str "mpd-ppc-gas_dosing_exch_" ch "_Value")}]]])

(defn target-select [spec-map opts]
  (into [:select.uk-select.exch-select
         (merge base-map spec-map)
         [:option {:value "-"} "-"]]
        (mapv (fn [{:keys [value display]}]
                [:option {:value value} (or display value)])
              opts)))
  
  (defn content [conf {:keys [active data]}]
    [:div.uk-container.uk-container-large.uk-padding-large
     ;; obs pressure
     [:div.uk-card.uk-card-body.uk-card-default
      (channel-value-input "ObservePressure" "Observer Pressure (DualGauge, mbar)")]

     [:div {:uk-grid ""}

      ;; pressure select
      [:div.uk-card.uk-card-body.uk-card-default
       [:h3.uk-card-title "Target Pressure"]
       [:div.uk-form-controls
        (target-select
         {:data-exchpath "TargetPressure"
          :data-type "float"}
         target-pressure-opts)]]
      ;; reservoir selection
      [:div.uk-card.uk-card-body.uk-card-default
       [:h3.uk-card-title "Target Reservoir"]
       [:div.uk-form-controls
        (target-select
         {:data-exchpath "TargetReservoir"
          :data-type "string"}
         target-reservoir-opts)]]
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
