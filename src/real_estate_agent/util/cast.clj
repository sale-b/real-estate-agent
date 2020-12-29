(ns real-estate-agent.util.cast)

(defn string-to-int [^String int]
  (Integer. int))

(defn string-to-long [^String long]
  (Long. long))

(defn string-to-double [^String double]
  (Double. double))