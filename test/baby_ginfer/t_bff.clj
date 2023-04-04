(ns baby-ginfer.t-bff
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all]))

;demonstrates referencing,
;and how events travel along a path of connected nodes

(fact
  "the friend of my friend is.. a liability?"

  (let [blueprints {"friends with"           (links-with "friend of")
                    "under phishing attack?" (inferred-with identity
                                                            [["friends with" "under phishing attack?"]])}
        events [(update-node "me" "friends with" "alice")
                (update-node "alice" "friends with" "bob")
                (update-node "bob" "under phishing attack?" true)]
        final-state (infer blueprints events)]

    ;bob is compromised, therefore I'm compromised
    (get-in final-state [:nodes "me" "under phishing attack?"]) => truthy))
