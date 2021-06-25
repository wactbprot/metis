(ns  metis.srv.handler
    ^{:author "wactbprot"
      :doc "Handler functions."}
  (:require [metis.stmem.interface :as stmem]
            [metis.tasks.interface :as tasks]))

(defn req->mp-id [req] (get-in req [:route-params :mp-id] "*"))

(defn req->active-param [req] (get-in req [:params :active] 0))

(defn assoc-tasks [m] (assoc m :task (tasks/get-task m)))

(defn assoc-states [m]
  (let [states (stmem/get-maps (assoc m :func :state :seq-idx :* :par-idx :*))]
    (assoc m :states (mapv assoc-tasks states))))

(defn assoc-descr [m]
  (assoc m :descr (stmem/get-map (assoc (dissoc m :seq-idx :par-idx) :func :descr))))

(defn assoc-title [m]
  (assoc m :title (stmem/get-map (assoc (dissoc m :seq-idx :par-idx) :func :title))))

(defn data [req]
  (let [mp-id  (req->mp-id req)
        ctrls  (stmem/get-maps {:mp-id mp-id :struct :cont :no-idx :* :func :ctrl})]
    {:mp-id mp-id
     :active (req->active-param req)
     :data (mapv (comp assoc-states assoc-descr assoc-title) ctrls)}))
    
