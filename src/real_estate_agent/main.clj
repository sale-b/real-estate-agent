(ns real-estate-agent.main
  (:require [real-estate-agent.db.migration :refer [migrate-up! migrate-down!]]
            [real-estate-agent.db.seed :refer [insert-all-seeds!]]
            [real-estate-agent.rest.rest :as rest]
            [environ.core :refer [env]]
            [real-estate-agent.util.cast :refer [string-to-int]])
  (:use ring.adapter.jetty
        real-estate-agent.service.jobs)
  (:gen-class))

(defn -main
  [& args]
  (migrate-down!)
  (migrate-up!)
  (insert-all-seeds!)
  ;(jobs)
  (run-jetty rest/app {:port (string-to-int (:port env))})
  )
