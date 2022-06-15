(ns  metis.srv.handler
  ^{:author "wactbprot"
    :doc "Handler functions."}
  (:require [metis.exchange.interface :as exch]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]            
            [metis.tasks.interface :as tasks]
            [clojure.string :as string]))


(defn registered-mps []
  (filterv
   #(string/starts-with? % "mpd")
   (map #(:mp-id (stmem/key->map (:reg-key %)))
        (stmem/registered))))

(defn running? [mp-id] (contains? (set (registered-mps)) mp-id))
  
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

(defn cont [req]
  (prn (req->active-param req))
  (let [mp-id  (req->mp-id req)
        ctrls  (stmem/get-maps {:mp-id mp-id :struct :cont :no-idx :* :func :ctrl})]
    {:mp-id mp-id
     :descr (stmem/get-val {:mp-id mp-id :struct :meta :metapath :descr})
     :running (running? mp-id)
     :active (req->active-param req)
     :data (mapv (comp assoc-states assoc-descr assoc-title) ctrls)}))

(defn elem [req]
  (prn (req->active-param req))
  (let [mp-id  (req->mp-id req)
        elems  (stmem/get-maps {:mp-id mp-id :struct :cont :no-idx :* :func :elem})]
    {:mp-id mp-id
     :descr (stmem/get-val {:mp-id mp-id :struct :meta :metapath :descr})
     :running (running? mp-id)
     :active (req->active-param req)
     :all-exch (exch/all {:mp-id mp-id})
     :data (mapv (comp assoc-descr assoc-title) elems)}))

(defn task-deps [mp-id]
  (mapv (fn [s] {:task-name s
                 :available (map? (ltmem/get-task s))})
        (stmem/get-val {:mp-id mp-id :struct :meta :metapath :task-deps})))

(defn mp-deps [mp-id]
  (mapv (fn [s]{:mp-id s
                :running (running? s)})
        (stmem/get-val {:mp-id mp-id :struct :meta :metapath :mp-deps})))

(defn home [req]
  (let [registered (registered-mps)]
    (mapv (fn [mp-id]
            {:mp-id mp-id
             :name (stmem/get-val {:mp-id mp-id :struct :meta :metapath :name})
             :descr (stmem/get-val {:mp-id mp-id :struct :meta :metapath :descr})
             :task-deps (task-deps mp-id)
             :mp-deps (mp-deps mp-id)})
          registered)))
