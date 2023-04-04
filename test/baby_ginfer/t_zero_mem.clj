(ns baby-ginfer.t-zero-mem
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all])
  (:import (java.io File)))

;demonstrates using the fs for persistence rather than holding state in-mem

(def tmp-file
  (.getAbsolutePath (File/createTempFile "baby" ".ginfer")))

;naively load/store the data in its entirety every time, this is just a demo after all
(defn load-data [] (read-string (slurp tmp-file)))
(def store-data (partial spit tmp-file))
(def fs-connector
  {:load-fn  (fn [cache node attribute]
               (get-in (load-data) [node attribute]))
   :store-fn (fn [cache node attribute value]
               (store-data (assoc-in (load-data) [node attribute] value)))})

(against-background
  [(around :facts (do (store-data {}) ?form #_delete?))]
  (fact
    "no data is held in memory"

    (let [blueprints {"connects to"         (links-with "connected from")
                      "data point"          (generic)
                      "inferred data point" (inferred-with (fnil inc 0) [["connects to" "data point"]])}
          events [(update-node "some node" "connects to" "another node")
                  (update-node "another node" "data point" 2)]]

      (infer blueprints events fs-connector)
      (get-in (load-data) ["some node" "inferred data point"]) => 3)))
