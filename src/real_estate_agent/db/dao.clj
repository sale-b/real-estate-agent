(ns real-estate-agent.db.dao
  (:import [java.sql PreparedStatement])
  (:require [clojure.java.jdbc :as db]
            [environ.core :refer [env]]
            [clj-time.coerce :as c])
  (:use ring.util.response))

(extend-type java.util.Date
  db/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (c/to-sql-time v))))

(def db
  {:dbtype   (:database-type env)
   :dbname   (:database-name env)
   :user     (:database-username env)
   :password (:database-password env)
   :host     (:database-host env)
   :port     (:database-port env)})


(defn get-user
  [id]
  (first (db/query db
            ["select * from users where id = ?" id])))
