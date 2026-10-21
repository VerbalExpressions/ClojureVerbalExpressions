(ns clojureverbalexpressions.test-runner
  (:require [cljs.test :as t]
            [clojureverbalexpressions.core-test]))

(defmethod t/report [::t/default :end-run-tests] [m]
  (set! (.-exitCode js/process) (if (t/successful? m) 0 1)))

(defn -main []
  (t/run-tests 'clojureverbalexpressions.core-test))

(set! *main-cli-fn* -main)
