(ns real-estate-agent.main
  (:require [real-estate-agent.db.migration :refer [migrate-up! migrate-down!]]
            [real-estate-agent.db.seed :refer [insert-all-seeds!]]
            [real-estate-agent.rest.rest :as rest]
            [environ.core :refer [env]]
            [real-estate-agent.util.cast :refer [cast-int]])
  (:use ring.adapter.jetty)
  (:gen-class))

(defn -main
  [& args]
  (migrate-down!)
  (migrate-up!)
  (insert-all-seeds!)
  (run-jetty rest/app {:port (cast-int (:port env))})
  )
