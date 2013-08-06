ClojureVerbalExpressions
=======================

## Installation
To be done...

## Usage
```clojure
user=> (use 'clojureverbalexpressions.core)
nil
user=> (def verbal VerEx)
#'user/verbal
```
## Examples

### Testing if we have a valid URL
```clojure
;; Create an example of how to test for correctly formed URLs

(def tester  (-> VerEx 
                 (start-of-line)
                 (find "http")
                 (maybe "s")
                 (anything-but " ")
                 (end-of-line)))


;; Create an example URL
(def test-url "https://www.google.com")

;; Test if the URL is valid
(if (match tester test-url)
  (println "Valid URL"))

;; Print the generated regex
(println (source tester)) 
;; => ^(http)(s)?(\:\/\/)(www\.)?([^\ ]*)$
```
### Replacing strings
```clojure
;; Create a test string
(def replace-me "Replace bird with a duck")

;; Create an expression that looks for the word "bird"
(def expression (VerEx (find "bird")))

;; Execute the expression in VerEx
(def result-verex (replace expression replace-me "duck"))
(println result-verex)
```
### Shorthand for string replace
```clojure
(def result (replace (VerEx (find "red")) "We have a red house" "blue"))
(println result)
```
