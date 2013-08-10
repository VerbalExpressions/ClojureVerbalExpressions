(ns clojureverbalexpressions.core
  (:require [clojure.string :as s])
  (:refer-clojure :exclude [find replace range or]))

(defrecord VerbalExpression [source modifier prefix suffix pattern])

;; The VE should start matching on all lines.
(def VerEx (VerbalExpression. "" "m" "" "" #"(?m)"))

(defn replace [{regex :pattern} string replacement]
  (s/replace string regex replacement))

(defn regex [{regex :pattern}]
  regex)

(defn source [{source :source}]
  source)

(defn match [{regex :pattern} string]
  (if (nil? (re-find regex string))
    false
    true))

(defn sanitize [string]
  (s/replace string #"([.$*+?^()\[\]{}\\|])" "\\\\$1"))

(defn add [{:keys [prefix source suffix modifier] :as v} value]
  ;; Debuging proposes
  ;;(println (str "(?" modifier ")" prefix source value suffix))
  (assoc v
         :pattern (re-pattern (str "(?" modifier ")" prefix source value suffix))
         :source (str source value)))

(defn anything [verex]
  (add verex "(?:.*)"))

(defn anything-but [verex value]
  (add verex (str "(?:[^" (sanitize value) "]*)")))

(defn something [v value]
  (add v "(?:.+)"))

(defn something-but [v value]
  (add v (str "(?:[^" (sanitize value) "]+)")))

(defn end-of-line [{suffix :suffix :as verex}]
  (add (assoc-in verex [:suffix] (str suffix "$")) ""))

(defn maybe [verex value]
  (add verex (str "(?:" (sanitize value) ")?")))

(defn start-of-line [{prefix :prefix :as verex}]
  (add (assoc-in verex [:prefix] (str "^" prefix)) ""))

(defn find [verex value]
  (add verex (str "(?:" (sanitize value) ")")))

(def then find)

(defn any [verex value]
  (add verex (str "[" (sanitize value) "]")))

(def any-of any)

(defn line-break [verex]
  (add verex "(?:(?:\\n)|(?:\\r\\n))"))

(def br line-break)

(defn range [verex & args]
  (let [from-tos (partition 2 (for [i args] (sanitize i)))]
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

(defn add-modifier [{modifier :modifier :as v}  m]
  (-> (assoc v :modifier (str m modifier))
      (add "")))

(defn remove-modifier [{modifier :modifier :as v} m]
  (-> (assoc v :modifier (s/replace modifier m ""))
      (add "")))

(defn multiple [v value]
  (let [value (sanitize value)]
    (add v (case (last value) (\* \+) value (str value "+")))))

(defn with-any-case
  ([v]
     (with-any-case v true))
  ([v b]
     (if b (add-modifier v "i") (remove-modifier v "i"))))

(defn search-one-line
  ([v]
     (search-one-line v true))
  ([v b]
     ;; As the VE does matches on all lines, we need to remove the
     ;; modifier when we want to select on one line.
     (if b (remove-modifier v "m") (add-modifier v "m"))))

(defn begin-capture [{suffix :suffix :as v}]
  (-> (assoc v :suffix (str suffix ")"))
      (add "(")))

(defn end-capture [{suffix :suffix :as v}]
  (-> (assoc v :suffix (subs suffix 0 (dec (count suffix))))
      (add ")")))
