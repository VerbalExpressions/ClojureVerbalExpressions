(ns clojureverbalexpressions.core-test
  (:require [clojure.test :refer [deftest is]]
            [clojureverbalexpressions.core :as verex]))

(deftest verex-renders-as-strings
  (let [v (-> verex/VerEx (verex/add "^$"))]
    (is (= "^$" (verex/source v)))))

(deftest matches-characters-in-range
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "c"))]
    (is (true? (verex/match v "a")))
    (is (true? (verex/match v "b")))
    (is (true? (verex/match v "c")))))

(deftest matches-characters-in-several-ranges
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "b" "X" "Z"))]
    (doseq [i ["a" "b"]]
      (is (true? (verex/match v i))))
    (doseq [i ["X" "Y" "Z"]]
      (is (true? (verex/match v i))))))

(deftest should-not-match-chars-outside-of-range
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/range "a" "b" "X" "Z"))]
    (is (false? (verex/match v "c")))
    (is (false? (verex/match v "W")))))

(deftest should-match-start-of-line
  (let [v (-> verex/VerEx (verex/start-of-line))]
    (is (true? (verex/match v "text  ")))))

(deftest should-match-end-of-line
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/end-of-line))]
    (is (false? (verex/match v "")))))

(deftest matches-anything
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/end-of-line))]
    (is (true? (verex/match v "!#¤%&/()=?`-,'*|")))))

(deftest should-match-anything-except-the-specified-element-when-not-found
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything-but "X") (verex/end-of-line))]
    (is (true? (verex/match v "Y Files")))))

(deftest should-not-match-anything-except-the-specified-element-when-found
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything-but "X") (verex/end-of-line))]
    (is (false? (verex/match v "VerEX")))))

(deftest finds-element
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/find "Wally") (verex/end-of-line))]
    (is (true? (verex/match v "Wally")))))

(deftest doesnt-find-element
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/find "Wally") (verex/end-of-line))]
    (is (false? (verex/match v "Wall-e")))))

(deftest match-when-the-element-is-maybe-there
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/find "Clojure1.") (verex/maybe "5") (verex/end-of-line))]
    (is (true? (verex/match v "Clojure1.5")))))

(deftest match-when-the-possible-element-is-gone
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/find "Clojure1.") (verex/maybe "5") (verex/end-of-line))]
    (is (true? (verex/match v "Clojure1.")))))

(deftest should-match-on-any-when-element-is-found
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/any "Q") (verex/anything) (verex/end-of-line))]
    (is (true? (verex/match v "Query")))))

(deftest should-not-match-on-any-when-element-is-not-found
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/any "Q") (verex/anything) (verex/end-of-line))]
    (is (false? (verex/match v "W")))))

(deftest should-match-when-line-break-is-there
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/line-break) (verex/anything) (verex/end-of-line))]
    (is (true? (verex/match v "Marco \r\n Polo")))))

(deftest should-not-match-when-line-breaks-are-none
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/line-break) (verex/anything) (verex/end-of-line))]
    (is (false? (verex/match v "Marco Polo")))))

(deftest should-match-when-tab-is-present
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/tab) (verex/end-of-line))]
    (is (true? (verex/match v "  \t")))))

(deftest should-not-match-when-tab-is-missing
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/tab) (verex/end-of-line))]
    (is (false? (verex/match v "Nope")))))

(deftest match-one-word
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/word) (verex/end-of-line))]
    (is (true? (verex/match v "OneWord")))))

(deftest not-match-when-two-words-are-present
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/word) (verex/end-of-line))]
    (is (false? (verex/match v "Two Words")))))

(deftest match-when-or-is-fulfilled
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/find "G") (verex/or) (verex/find "h") (verex/end-of-line))]
    (is (true? (verex/match v "Github")))))

(deftest should-not-match-when-or-condition-not-fulfilled
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/find "G") (verex/or) (verex/find "h") (verex/end-of-line))]
    (is (false? (verex/match v "Bitbucket")))))

(deftest should-match-on-upper-case-when-lower-case-is-given-and-any-case-is-true
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/find "THOR") (verex/end-of-line) (verex/with-any-case true))]
    (is (true? (verex/match v "thor")))))

(deftest should-match-multiple-lines
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/anything) (verex/find "Pong") (verex/anything) (verex/end-of-line) (verex/search-one-line false))]
    (is (true? (verex/match v "Ping \n Pong \n Ping")))))

(deftest should-match-email-address
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/word) (verex/then "@") (verex/word) (verex/then ".") (verex/word) (verex/end-of-line))]
    (is (true? (verex/match v "mail@mail.com")))))

(deftest should-match-url
  (let [v (-> verex/VerEx (verex/start-of-line) (verex/then "http") (verex/maybe "s") (verex/then "://") (verex/maybe "www.") (verex/word) (verex/then ".") (verex/word) (verex/maybe "/") (verex/end-of-line))]
    (is (true? (verex/match v "https://google.com")))))
