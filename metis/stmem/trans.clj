✔ (ns metis.stmem.trans
?   (:require [metis.stmem.core :as core]
?             [metis.config.interface :as c]
?             [cheshire.core :as che]
?             [clojure.string :as string]
?             [metis.utils.interface :as utils]))
  
? ;;------------------------------
? ;; map to key utils
? ;;------------------------------
✔ (defn pad-ok?
?   "Checks if the padding of `i` is ok. `\"*\"` serves pattern matching."
?   ([idx]
✔    (pad-ok? c/config idx))
?   ([config idx]
~    (cond
✔      (and (string? idx)
✔           (= (count idx)
✔              (:stmem-key-pad-length config))) true
✔      (= idx "*")                         true 
?      :else                               false)))
  
✔ (defn lpad
?   "Left pad the given number if it is not a string."
?   ([idx]
✘    (lpad c/config idx))
?   ([config idx]
✔    (if (pad-ok? idx)
✘      idx
✔      (when idx (format (str "%0" (:stmem-key-pad-length config) "d") (utils/ensure-int idx))))))
  
✔ (defn map->task-key
?   [{trans :stmem-trans s :stmem-key-sep} m]
✔   (let [task-name (:task-name m)]
✔     (str (:tasks trans) s (if (keyword? task-name) (task-name trans) task-name))))
  
✔ (defn map->struct-part
?   [{trans :stmem-trans s :stmem-key-sep} m]
✔   (when  (:mp-id m)
✔     (when-let [p (:struct m)]
~       (str (:mp-id m) s (if (keyword? p) (p trans) p)))))
  
✔ (defn map->no-idx-part
?   [{trans :stmem-trans s :stmem-key-sep :as config} m]
~   (when-let[no-idx (:no-idx m)]
✘     (str s (if (= :* no-idx) (:* trans) (lpad config (:no-idx m))))))
  
✔ (defn map->exch-part
?   [{s :stmem-key-sep :as config} m]
~   (when-let [p (:exch m)] (str s p)))
  
✔ (defn map->doc-id-part
?   [{trans :stmem-trans s :stmem-key-sep :as config} m]
✔   (when-let [p (:doc-id m)] (str s (if (keyword? p) (p trans) p))))
  
✔ (defn map->metapath-part
?   [{trans :stmem-trans s :stmem-key-sep} m]
~   (when-let [p (:metapath m)] (str s (if (keyword? p) (p trans) p))))
  
✔ (defn map->func-part
?   [{trans :stmem-trans s :stmem-key-sep} m]
~   (when-let [p (:func m)] (str s (if (keyword? p) (p trans) p))))
  
✔ (defn map->seq-par-idx-part
?   [{trans :stmem-trans s :stmem-key-sep :as config} m]
✔   (let [sdx (:seq-idx m)
✔         pdx (:par-idx m)]
~     (when sdx
✘     (str s (if (number? sdx) (lpad config sdx) (:* trans))
✘          (when pdx
✘            (str s (if (number? pdx) (lpad config pdx) (:* trans))))))))
  
✔ (defn map->def-key
?   [config m]
✔   (str
✔    (map->struct-part config m)
✔    (str ;this str only groups the next parts
✔     (map->no-idx-part config m)
✔     (map->exch-part config m)
✔     ( map->doc-id-part config m)
✔     (map->metapath-part config m)) 
✔    (map->func-part config m)
✔    (map->seq-par-idx-part config m)))
  
✔ (defn map->key
?   ([m]
✔    (map->key c/config m))
?   ([config m]
✔    (when (and (map? m) (seq m)) 
✔      (if (:task-name m)
✔          (map->task-key config m)
✔          (map->def-key config m)))))
  
  
? ;;------------------------------
? ;; key to map  utils
? ;;------------------------------
✔ (defn care-no-idx [config v]
✔   (when-let [s (get v 2)]
✔     (if (re-matches #"[0-9]*" s) (utils/ensure-int s) s)))
  
✔ (defn care-struct [{retrans :stmem-retrans} v] (get retrans  (get v 1)))
  
✔ (defn key->map
?   ([k]
✔    (key->map c/config k))
?   ([{re-sep :re-sep retrans :stmem-retrans :as config} k]
✔    (when k
✔      (when-let [v (string/split k re-sep)]
✔        {:mp-id (get v 0)
✔         :struct (care-struct config v)
✔         :no-idx (care-no-idx config v)
✔         :func (get retrans (get v 3))
✔         :seq-idx (utils/ensure-int (get v 4))
✔         :par-idx (utils/ensure-int (get v 5))}))))
?   
