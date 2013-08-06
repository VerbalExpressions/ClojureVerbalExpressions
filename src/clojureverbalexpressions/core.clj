(ns clojureverbalexpressions.core
  (:require [clojure.string :as s])
  (:refer-clojure :exclude [find replace range]))


(def modsmap (atom {:I false :M false}))

(defrecord VerEx [regex])
 

(defprotocol VerExProt
  "lol"
  (replace [regex string replacement] "heh"))


(extend-protocol VerExProt
  VerEx
  (replace [{regex :regex :as expr} string replacement]
    (s/replace string regex replacement)))

(defn verex  [& args]
  (let [mods (filter string? (vals modsmap))]
    (VerEx. (re-pattern (s/join "" (cons mods args))))))

(defn anything  []
    "(.*)")

(defn anything-but [value]
  (str "([^" value "]*)"))

(defn end-of-line []
  "$")

(defn maybe [value]
  (str "(" value ")?"))

(defn start-of-line  []
    "^")

(defn find  [value]
    (str "(" value ")"))

(defn any [value]
  (str "([" value "])"))

(defn line-break  []
    "(\\n|(\\r\\n))")

(defn range [& args]
  (let [from-tos (partition 2 args)]
    (str "([" (s/join "" (for [i from-tos] (s/join "-" i))) "])")))

(defn tab  []
    "\t")

(defn word []
    "(\\w+)")

(defn OR [& {:keys [value]
             :or {value nil}}]
  (str "|" (if value (find value))))


(defn with-any-case [& {:keys [value]
                        :or {value false}}]
  (if value
    (swap! @modsmap update-in :I "(?u)")
    (swap! @modsmap update-in :I false)))

(defn search-one-line [& {:keys [value]
                          :or {value false}}]
  (if value
    (swap! @modsmap update-in :M "(?m)")
    (swap! @modsmap update-in :M false)))
