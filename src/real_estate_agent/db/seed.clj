(ns real-estate-agent.db.seed
  (:import [java.sql PreparedStatement])
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [clj-time.coerce :as c]
            [clojure.edn :as edn]
            [real-estate-agent.db.dao :refer [db]]))

(extend-type java.util.Date
  db/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (c/to-sql-time v))))

(defn insert-seed!
  "Inserts a single seed definition into the database."
  [seed]
  (doseq [{:keys [table data]} seed]
    (db/insert-multi! db table data)))

(defn insert-all-seeds!
  "Reads all files in the seeds directory and inserts their contents into
   the database."
  []
  (->> (.listFiles (io/file (io/resource "seeds")))
       (map slurp)
       (map edn/read-string)
       (map insert-seed!)
       doall))
