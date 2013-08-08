(ns clojureverbalexpressions.core
  (:require [clojure.string :as s])
  (:refer-clojure :exclude [find replace range or]))


(defrecord RecVerEx [source modifier prefix suffix pattern])


(def VerEx (RecVerEx. "" "" "" "" #""))


(defprotocol IVerEx
  "Protocol for the RecVerEx record"
  (replace [src string replacement] "Replaces a given string with the replacment if the regex is a match")
  (regex [src] "Gives the regex")
  (source [src] "Returns the source as a string")
  (match [src string] "Checks if the regex is a match according too the given string." ))


(extend-protocol IVerEx
  RecVerEx
  (replace [{regex :pattern :as v} string replacement]
    (s/replace string regex replacement))
  (regex [{regex :pattern :as v}]
    regex)
  (source [{source :source :as v}]
    source)
  (match [{regex :pattern :as v} string]
    (if (nil? (re-find regex string))
      false
      true)))

(def esc-chars {\. "\\." 
                \^ "\\^"
                \$ "\\$"
                \* "\\*"
                \+ "\\+"
                \? "\\?"
                \( "\\("
                \) "\\)"
                \[ "\\["
                \] "\\]"
                \{ "\\{"
                \} "\\}"
                \\ "\\\\"
                \| "\\|"})

(defn re-escaper [string]
  (clojure.string/escape string esc-chars))


(defn add [{:keys [prefix source suffix modifier] :as v} value]
  ;; Debuging proposes
  ;;(println (str "(?" modifier ")" prefix source value suffix))
  (assoc v
         :pattern (re-pattern (str "(?" modifier ")" prefix source value suffix))
         :source (str source value)))


(defn anything [verex]
  (add verex "(?:.*)"))

(defn anything-but [verex value]
  (add verex (str "(?:[^" (re-escaper value) "]*)")))

(defn end-of-line [{suffix :suffix :as verex}]
  (add (assoc-in verex [:suffix] (str suffix "$")) ""))

(defn maybe [verex value]
  (add verex (str "(?:" (re-escaper value) ")?")))

(defn start-of-line [{prefix :prefix :as verex}]
  (add (assoc-in verex [:prefix] (str "^" prefix)) ""))

(defn find [verex value]
  (add verex (str "(?:" (re-escaper value) ")")))

(def then find)

(defn any [verex value]
  (add verex (str "(?:[" (re-escaper value) "])")))

(defn line-break [verex]
  (add verex "(?:(?:\\n)|(?:\\r\\n))"))

(defn range [verex & args]
  (let [from-tos (partition 2 (for [i args] (re-escaper i)))]
    (add verex (str "([" (s/join "" (for [i from-tos] (s/join "-" i))) "])"))))

(defn tab [verex]
  (add verex "\t"))

(defn word [verex]
  (add verex "\\w+"))

(defn or
  ([{:keys [prefix suffix] :as v}]
   (-> (assoc v :prefix (str prefix "(?:") :suffix (str ")" suffix))
       (add ")|(?:")))

  ([v value]
   (then (or v) value)))


(defn remove-modifier [{modifier :modifier :as v} modi]
  (let [new-str (clojure.string/replace modifier (re-pattern modi) "")]
    (assoc v :modifier new-str)))




(defn with-any-case
  ([v]
     (with-any-case v true))
  ([v b]
     (if b (add-modifier v "i") (remove-modifier v "i"))))

(defn search-one-line
  ([v]
     (search-one-line v true))
  ([v b]
     (if b (remove-modifier v "m") (add-modifier v "m"))))
