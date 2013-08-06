(ns clojureverbalexpressions.core
  (:require [clojure.string :as s])
  (:refer-clojure :exclude [find replace range]))


(def modsmap (atom {:I false :M false}))


(defrecord RecVerEx [src])


(def VerEx (RecVerEx. []))


(defn make-source [{src :src :as rec}]
  (let [mods (filter string? (vals @modsmap))]
    (println src)
    (s/join "" (concat mods src))))


(defprotocol IVerEx
  "lol"
  (replace [src string replacement] "heh")
  (regex [src] "Gives the regex")
  (source [src] "returns the source"))


(extend-protocol IVerEx
  RecVerEx
  (replace [{src :src :as expr} string replacement]
    (let [regexsrc (re-pattern (make-source src))]
      (s/replace string regexsrc replacement)))
  (regex [{src :src :as expr}]
    (let [mods (filter string? (vals @modsmap))]
      (s/join "" (concat mods src))))
  (source [{src :src :as expr}]
    (let [mods (filter string? (vals @modsmap))]
      (re-pattern (s/join "" (concat mods src))))))


;; Because we lazy
(defn update-record [rec rule]
  (update-in rec [:src] conj rule))

(defmacro defrule [nm args & args-body]
  (let [record (first args)]
   `(defn ~nm ~args
     (update-record ~record ~@args-body))))


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

(def tester  (-> VerEx 
                 (start-of-line)
                 (find "http")
                 (maybe "s")
                 (anything-but " ")
                 (end-of-line)))



