(ns clojureverbalexpressions.core-test
  (:use midje.sweet)
  (:require [clojureverbalexpressions.core :as verex]))


(fact "verex renders as strings"
      (def v (-> verex/VerEx (verex/add "^$") (verex/source)))
      v => "^$")

(fact "matches characters in range"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "c")))
      (verex/match v "a") => true
      (verex/match v "b") => true
      (verex/match v "c") => true)

(fact "does not match characters outside of range"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "b" "X" "Z")))
      (doseq [i ["a" "b"]]
        (verex/match v i) => true)
      )








