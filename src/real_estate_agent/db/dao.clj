(ns real-estate-agent.db.dao
  (:require [clojure.java.jdbc :as db]
            [environ.core :refer [env]])
  (:use ring.util.response))

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
            ["select * from users where id = ?::integer" id])))
