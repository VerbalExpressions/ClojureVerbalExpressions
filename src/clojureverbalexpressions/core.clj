(ns clojureverbalexpressions.core
  (:require [clojure.string :as s])
  (:refer-clojure :exclude [find replace range]))


(def modsmap (atom {:I false :M false}))


(defrecord RecVerEx [src])


(def VerEx (RecVerEx. []))


(defn make-source [src]
  (let [mods (filter string? (vals @modsmap))]
    (s/join "" (concat mods src))))


(defprotocol IVerEx
  "Protocol for the RecVerEx record"
  (replace [src string replacement] "Replaces a given string with the replacment if the regex is a match")
  (regex [src] "Gives the regex")
  (source [src] "Returns the source as a string")
  (match [src string] "Checks if the regex is a match according too the given string." ))


(extend-protocol IVerEx
  RecVerEx
  (replace [{src :src :as expr} string replacement]
    (let [regexsrc (re-pattern (make-source src))]
      (s/replace string regexsrc replacement)))
  (regex [{src :src :as expr}]
    (re-pattern (make-source src)))
  (source [{src :src :as expr}]
    (make-source src))
  (match [{src :src :as expr} string]
    (let [regex (re-pattern (make-source src))]
      (if (nil? (re-matches regex string))
        false
        true))))


;; Because we lazy
(defn update-record [rec rule]
  (update-in rec [:src] conj rule))

(defmacro defrule [nm args & args-body]
  (let [record (first args)]
   `(defn ~nm ~args
     (update-record ~record ~@args-body))))

(defrule add [verex value]
  (str value))

(defrule anything [verex]
  "(.*)")


(defrule anything-but [verex value]
  (str "([^" value "]*)"))


(defrule end-of-line [verex]
  "$")


(defrule maybe [verex value]
  (str "(" value ")?"))


(defrule start-of-line  [verex]
    "^")


(defrule find  [verex value]
    (str "(" value ")"))


(defrule any [verex value]
  (str "([" value "])"))


(defrule line-break  [verex]
    "(\\n|(\\r\\n))")


(defrule range [verex & args]
  (let [from-tos (partition 2 args)]
    (str "([" (s/join "" (for [i from-tos] (s/join "-" i))) "])")))



(defrule tab  [verex]
    "\t")


(defrule word [verex]
    "(\\w+)")

;; or is a keyword
(defrule OR [verex & {:keys [value]
                      :or {value nil}}]
  (str "|" (if value (find value))))


(defn with-any-case [verex & {:keys [value]
                              :or {value false}}]
  (if value
    (swap! modsmap assoc-in :I "(?u)")
    (swap! modsmap assoc-in :I false)))


(defn search-one-line [& {:keys [value]
                          :or {value false}}]
  (if value
    (swap! modsmap assoc-in [:M] "(?m)")
    (swap! modsmap assoc-in [:M] false)))


