(ns baby-ginfer.t-raw
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all]))

;demonstrates blueprints are just maps, removed of sugar

(fact
  "blueprints are just maps after all"

  (let [safe-inc (fnil inc 0)
        blueprints {"connects to"         (links-with "connected from")
                    "data point"          (generic)
                    "inferred data point" (inferred-with safe-inc [["connects to" "data point"]])}
        final-state (infer blueprints nil)]

    (:blueprints final-state) => {"connected from"      {:ref "connects to"}
                                  "connects to"         {:listeners '(["inferred data point"]) :ref "connected from"}
                                  "data point"          {:listeners '(["connected from" "inferred data point"])}
                                  "inferred data point" {:eval-fn safe-inc
                                                         :sources [["connects to" "data point"]]}}))
