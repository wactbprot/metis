(ns metis.page.mpd-ppc-gas_dosing
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides a special io page for mpd-ppc-gas_dosing."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))

(def mp-id "mpd-ppc-gas_dosing")

(def target-pressure-opts 
  [{:value 0 :display "0"}
   {:value 0.1 :display "0.1"}
   {:value 0.2 :display "0.2"}
   {:value 1 :display "1"}
   {:value 2 :display "2"}
   {:value 3 :display "3"}])


(def default-opt [:option {:value 0} "select"])

(defn id->
  ([exchpath exchkey] (id-> mp-id exchpath exchkey)) 
  ([mpd exchpath exchkey] (str mpd "_exch_" exchpath "_" exchkey)))

(defn input
  ([exchpath         label all-exch] (input exchpath "Value" label all-exch))
  ([exchpath exchkey label all-exch]
   (let [id (id-> exchpath exchkey)]
     [:div
      [:label.uk-form-label {:for id} label]
      [:div.uk-form-controls
       [:input.uk-input.exch-input
        {:type "text" :id id :value (get-in all-exch [exchpath (keyword exchkey)])}]]])))

(defn select [exchpath opts all-exch]
  (let [selected-val (get-in all-exch [exchpath :Selected])
        default-opt [:option {:value selected-val} selected-val]
        select-map  {:data-mp-id  mp-id
                     :data-struct "exch"
                     :data-exchpath exchpath}]
    (into [:select.uk-select.exch-select select-map default-opt]
          (mapv (fn [{:keys [value display]}]
                  [:option {:value value} (or display value)])
                opts))))

(defn content [conf {:keys [all-exch]}]
  [:div.uk-container.uk-padding-large.uk-container-large
   
   [:div {:uk-grid ""}
    
    ;; ctrl container
    [:button.uk-button.uk-button-default.uk-button-danger.ctrl-btn.uk-width-1-5
     {:data-mp-id mp-id
      :data-struct "cont"
      :data-no-idx "0"
      :data-func "ctrl"
      :data-value "run"} "Initialize Devices"]
    
    [:button.uk-button.uk-button-default.ctrl-btn.uk-width-1-5
     {:data-mp-id mp-id
      :data-struct "cont"
      :data-no-idx "1"
      :data-func "ctrl"
      :data-value "run"} "Open TMP Valve"]

    [:button.uk-button.uk-button-default.ctrl-btn.uk-width-1-5
     {:data-mp-id mp-id
      :data-struct "cont"
      :data-no-idx "2"
      :data-func "ctrl"
      :data-value "run"} "Close TMP Valve"]

    [:button.uk-button.uk-button-default.uk-button-primary.ctrl-btn.uk-width-1-5
     {:data-mp-id mp-id
      :data-struct "cont"
      :data-no-idx "3"
      :data-func "ctrl"
      :data-value "mon"} "Start Gas Dosing"]
    
    [:button.uk-button.uk-button-default.uk-button-secondary.ctrl-btn.uk-width-1-5
     {:data-mp-id mp-id
      :data-struct "cont"
      :data-no-idx "3"
      :data-func "ctrl"
      :data-value "stop"} "Stop Gas Dosing"]

    [:button.uk-button.uk-button-default.uk-width-1-5 {:id "mpd-ppc-gas_dosing_cont_0_ctrl" :disabled ""} "~"]
    [:button.uk-button.uk-button-default.uk-width-1-5 {:id "mpd-ppc-gas_dosing_cont_1_ctrl" :disabled ""} "~"]
    [:button.uk-button.uk-button-default.uk-width-1-5 {:id "mpd-ppc-gas_dosing_cont_2_ctrl" :disabled ""} "~"]
    [:button.uk-button.uk-button-default.uk-width-1-5 {:id "mpd-ppc-gas_dosing_cont_3_ctrl" :disabled ""} "~"]
    [:button.uk-button.uk-button-default.uk-width-1-5 {:disabled ""}]]
    
   [:div {:uk-grid ""}

    [:div.uk-card.uk-card-body.uk-card-default
     (input "ObservePressure" "Value" "Observer Pressure in mbar" all-exch)]
    [:div.uk-card.uk-card-body.uk-card-default
     (input "Servo_PPC_Pos" "Value" "Position of TMP Valve" all-exch)]
    [:div.uk-card.uk-card-body.uk-card-default
     (input "PPCVATDosingValve" "Mode" "VAT-Valve Operation Mode" all-exch)]
    [:div.uk-card.uk-card-body.uk-card-default
     (input "PPCVATDosingValve" "Position" "Position of VAT-Valve" all-exch)]]
   [:div {:uk-grid ""}

    ;; pressure select
    [:div.uk-card.uk-card-body.uk-card-default
     [:h3.uk-card-title "Target Pressure in mbar"]
     [:div.uk-form-controls
      (select "TargetPressure" target-pressure-opts all-exch)]]
    
    ;; pressure indication
    [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
     [:h3.uk-card-title "Reservoir 3"]
     (input "CH1" "1000T CDG (Ch1, mbar)" all-exch)
     (input "CH2" "10T CDG (Ch2, mbar)" all-exch)]
    [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
     [:h3.uk-card-title "Reservoir 4"]
     (input "CH3" "10T CDG (Ch3, mbar)" all-exch)
     (input "CH4" "0.1T CDG (Ch4, mbar)" all-exch)]
    [:div.uk-card.uk-card-body.uk-card-default.uk-card-small
     [:h3.uk-card-title "Reservoir 5"]
     (input "CH5" "10T CDG (Ch5, mbar)" all-exch)
     (input "CH6" "0.1T CDG (Ch6, mbar)" all-exch)]]])
