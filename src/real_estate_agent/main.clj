(ns real-estate-agent.main
  (:require [real-estate-agent.db.migration :refer [migrate-up! migrate-down!]]
            [real-estate-agent.db.seed :refer [insert-all-seeds!]])
  (:gen-class))

(defn -main
  [& args]
  (migrate-down!)
  (migrate-up!)
  ;(insert-all-seeds!)
  )
