(ns baby-ginfer.t-pets-life
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all]))

;demonstrates order-agnostic events leading to consistent outcome

(tabular

  (fact
    "Dog's owners always get flees"

    (let [blueprints {"person's pet"      (links-with "dog's owner")
                      "dog has flees?"    (generic)
                      "person has flees?" (inferred-with boolean
                                                         [["person's pet" "dog has flees?"]])}
          state (infer blueprints ?events)]

      ;poor joe gets flees in any outcome
      (get-in state [:nodes "joe" "person has flees?"]) => truthy))

  ?events

  ;1. link joe->snoopy (via person's pet)
  ;2. snoopy gets flees
  [(update-node "joe" "person's pet" "snoopy")
   (update-node "snoopy" "dog has flees?" true)]

  ;1. link snoopy->joe (via dog's owner)
  ;2. snoopy gets flees
  [(update-node "snoopy" "dog's owner" "joe")
   (update-node "snoopy" "dog has flees?" true)]

  ;1. snoopy gets flees
  ;2. link joe->snoopy (via person's pet)
  [(update-node "snoopy" "dog has flees?" true)
   (update-node "joe" "person's pet" "snoopy")])
