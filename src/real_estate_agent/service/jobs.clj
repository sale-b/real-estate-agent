(ns real-estate-agent.service.jobs
  (:use overtone.at-at))


(defn job-one [tp]
  (every 10000 #(println "Job one") tp)
  )

(defn job-two [tp]
  (every 5000 #(println "Job two") tp)
  )

(defn jobs []
  (def tp (mk-pool))
  (job-one tp)
  (job-two tp))