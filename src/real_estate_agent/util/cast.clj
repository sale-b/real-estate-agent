(ns real-estate-agent.util.cast)

(defn string-to-int [^String int]
  (if (nil? int)
    nil
    (Integer. int)))


(defn string-to-long [^String long]
  (if (nil? long)
    nil
    (Long. long)))


(defn string-to-double [^String double]
  (if (nil? double)
    nil
    (Double. double)))
