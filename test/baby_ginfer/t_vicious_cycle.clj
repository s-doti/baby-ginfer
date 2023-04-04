(ns baby-ginfer.t-vicious-cycle
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all]))

;demonstrates a case of cyclic inference

(tabular
  (fact
    "eval-notify vicious cycle"

    (let [blueprints {"socioeconomic status" (inferred-with (partial / 1)
                                                            [["poverty rates"]])
                      "poverty rates"        (inferred-with (partial / 1)
                                                            [["socioeconomic status"]])}]
      (-> blueprints
          (infer [?cause])
          (get-in ?affected-path)) => ?effect))

  ?cause
  ?affected-path ?effect

  ;high poverty rates => low socioeconomic status
  (update-node "population" "poverty rates" 100.0)
  [:nodes "population" "socioeconomic status"] 0.01

  ;low socioeconomic status => high poverty rates
  (update-node "population" "socioeconomic status" 0.01)
  [:nodes "population" "poverty rates"] 100.0)
