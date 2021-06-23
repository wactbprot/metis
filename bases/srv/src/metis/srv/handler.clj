(ns  metis.srv.handler
    ^{:author "wactbprot"
      :doc "Handler functions."}
    (:require [metis.stmem.interface :as stmem]))

(defn req->mp-id [req] (get-in req [:route-params :mp-id] "*"))

(defn assoc-states [m]
  (assoc m :states (stmem/get-maps (assoc m :func :state :seq-idx :* :par-idx :*))))

(defn assoc-descr [m]
  
  (assoc m :descr (stmem/get-map (assoc (dissoc m :seq-idx:par-idx) :func :descr))))

(defn data [req]
  (let [mp-id  (req->mp-id req)
        ctrls  (stmem/get-maps {:mp-id mp-id :struct :cont :no-idx :* :func :ctrl})]
    {:mp-id mp-id
     :data (mapv (comp assoc-states assoc-descr) ctrls)}))
    
