(ns metis.page.mpd-ppc-gas_dosing
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides a special io page for mpd-ppc-gas_dosing."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))


(def base-map {:data-mp-id "mpd-ppc-gas_dosing"
               :data-struct "exch"})

(def target-pressure-opts 
  [{:value 0.1 :display "0.1"}
   {:value 0.1 :display "0.2"}
   {:value 1 :display "1"}
   {:value 2 :display "2"}
   {:value 3 :display "3"}])

(def target-reservoir-opts
  [{:value 3 :display "3"}
   {:value 4 :display "4"}
   {:value 5 :display "5"}])

(def default-opt [:option {:value 0} "select"])

(defn exch-opt [all-exch keys]
  (let [x (get-in all-exch keys)]
    [:option {:value x} x]))

(defn input
  ([exchpath label]
   (input exchpath  "Value" label))
  ([exchpath exchkey label]
   (let [id (str "mpd-ppc-gas_dosing_exch_" exchpath "_" exchkey)]
     [:div
      [:label.uk-form-label {:for id} label]
      [:div.uk-form-controls
      [:input.uk-input.exch-input
       {:type "text" :id id}]]])))

(defn target-select
  ([spec-map opts]
   (target-select spec-map opts default-opt))
  ([spec-map opts default-opt]
   (into [:select.uk-select.exch-select (merge base-map spec-map) default-opt]
         (mapv (fn [{:keys [value display]}]
                 [:option {:value value} (or display value)])
               opts))))

(defn content [conf {:keys [all-exch]}]
  [:div.uk-container.uk-container-large.uk-padding-large
   
   [:div {:uk-grid ""}
    
    ;; obs pressure
    [:div.uk-card.uk-card-body.uk-card-default
     (input "ObservePressure" "Observer Pressure (DualGauge, mbar)")]
    [:div.uk-card.uk-card-body.uk-card-default
     (input "PPCVATDosingValve" "Mode" "VAT-Valve Operation Mode")]]
   
   [:div {:uk-grid ""}

    ;; pressure select
    [:div.uk-card.uk-card-body.uk-card-default
     [:h3.uk-card-title "Target Pressure in mbar"]
     [:div.uk-form-controls
      (target-select
       {:data-exchpath "TargetPressure"
        :data-type "float"}
       target-pressure-opts
       (exch-opt all-exch ["TargetPressure" :Selected]))]]
    
    ;; reservoir selection
    [:div.uk-card.uk-card-body.uk-card-default
     [:h3.uk-card-title "Target Reservoir"]
     [:div.uk-form-controls
      (target-select
       {:data-exchpath "TargetReservoir"
        :data-type "string"}
       target-reservoir-opts
       (exch-opt all-exch ["TargetReservoir" :Selected]))]]

    [:div {:uk-grid ""}
     ;; pressure indication
     [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
      [:h3.uk-card-title "Reservoir 3"]
      (input "CH1" "1000T CDG (Ch1, mbar)")
      (input "CH2" "10T CDG (Ch2, mbar)")]
     [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
      [:h3.uk-card-title "Reservoir 4"]
      (input "CH3" "10T CDG (Ch3, mbar)")
      (input "CH4" "0.1T CDG (Ch4, mbar)")]
     [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
      [:h3.uk-card-title "Reservoir 5"]
      (input "CH5" "10T CDG (Ch5, mbar)")
      (input "CH6" "0.1T CDG (Ch6, mbar)")]]]])
