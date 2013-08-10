ClojureVerbalExpressions
=======================

## Installation
Add `[clojureverbalexpression "0.2.1"]` too your project.clj file.

## Usage
```clojure
user=> (require '[clojureverbalexpressions :as verex])
nil
user=> (def verbal verex/VerEx)
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
                 (find "://")
                 (maybe "www.")
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
(def expression (find VerEx "bird"))

;; Execute the expression in VerEx
(def result-verex (replace expression replace-me "duck"))
(println result-verex)
```
### "Shorthand" for string replace
```clojure
;; (def result (clojure.string/replace "We have a red house" #"red" "blue")
(def result (replace (find VerEx "red") "We have a red house" "blue"))
(println result)
```
