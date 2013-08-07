(ns clojureverbalexpressions.core-test
  (:use midje.sweet)
  (:require [clojureverbalexpressions.core :as verex]))

;; nasty hack
(swap! verex/modsmap assoc-in [:I] false)
(swap! verex/modsmap assoc-in [:M] false)

(fact "verex renders as strings"
      (def v (-> verex/VerEx (verex/add "^$")))
       (verex/source v) => "^$")

(fact "matches characters in range"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "c")))
      (verex/match v "a") => true
      (verex/match v "b") => true
      (verex/match v "c") => true)

(fact "does not match characters outside of range"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "b" "X" "Z")))
      (doseq [i ["a" "b"]]
        (verex/match v i) => true)
      (doseq [i ["X" "Y" "Z"]]
        (verex/match v i) => true))

(fact "should not match chars outside of range"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "b" "X" "Z")))
      (verex/match v "c") => false
      (verex/match v "W") => false)


;; TODO This is probably not right
(fact "should match start of line"
      (def v (-> verex/VerEx (verex/start-of-line)))
      (verex/match v "text  ") => true)

(fact "should match end of line"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/end-of-line)))
      (verex/match v "") => true)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(fact "matches anything"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/end-of-line)))
      (verex/match v "!#¤%&/()=?`-,'*|") => true)

(fact "should match anything except the specefied element when not found"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything-but "X") (verex/end-of-line)))
      (verex/match v "Y Files") => true)

(fact "should match anything expect specefied element when found"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything-but "X") (verex/end-of-line)))
      (verex/match v "VerEX") => false)

(fact "finds element"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/find "Wally") (verex/end-of-line)))
      (verex/match v "Wally") => true)

(fact "dosnt find element"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/find "Wally") (verex/end-of-line)))
      (verex/match v "Wall-e") => false)

(fact "match when the element i maybe there"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/find "Clojure1.") (verex/maybe "5") (verex/end-of-line)))
      (verex/match v "Clojure1.5") => true)

(fact "match when the the possible element is gone"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/find "Clojure1.") (verex/maybe "5") (verex/end-of-line)))
      (verex/match v "Clojure1.") => true)

(fact "should match on any when element is found"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/any "Q") (verex/anything) (verex/end-of-line)))
      (verex/match v "Query") => true)

(fact "should not match on any when element is not found"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/any "Q") (verex/anything) (verex/end-of-line)))
      (verex/match v "W") => false)

(fact "should match when line break is there"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/line-break) (verex/anything) (verex/end-of-line)))
      (verex/match v "Marco \r\n Polo") => true)

(fact "should not match with line breaks are none"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/line-break) (verex/anything) (verex/end-of-line)))
      (verex/match v "Marco Polo") => false)

(fact "Should match when tab is present"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/tab) (verex/end-of-line)))
      (verex/match v "  \t") => true)

(fact "should not match when tab is missing"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/tab) (verex/end-of-line)))
      (verex/match v "Nope") => false)

(fact "match one word"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/word) (verex/end-of-line)))
      (verex/match v "OneWord") => true)

(fact "not match when two words are present"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/word) (verex/end-of-line)))
      (verex/match v "Two Words") => false)

(fact "match when or is fulfilled"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/find "G") (verex/OR) (verex/find "h") (verex/end-of-line)))
      (verex/match v "Github") => true)

(fact "should not match on upper case when or condition not fulfilled"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/find "G") (verex/OR) (verex/find "h") (verex/end-of-line)))
      (verex/match v "Bitbucket") => false)

(fact "should match on upper case when lower case is given and any case is true"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/find "THOR") (verex/end-of-line) (verex/with-any-case :value true)))
      (verex/match v "thor"))

(fact "should match multiple lines"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/find "Pong") (verex/anything) (verex/end-of-line) (verex/search-one-line :value true)))
      (verex/match v "Ping \n Pong \n Ping") => true)

(fact "should match email adress"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/word) (verex/then "@") (verex/word) (verex/then ".") (verex/word) (verex/end-of-line)))
      (verex/match v "mail@mail.com") => true)

(fact "should match url"
      (def v (-> verex/VerEx (verex/start-of-line) (verex/then "http") (verex/maybe "s") (verex/then "://") (verex/maybe "www.") (verex/word) (verex/then ".") (verex/word) (verex/maybe "/") (verex/end-of-line)))
      (verex/match v "https://google.com") => true)


