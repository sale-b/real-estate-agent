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

(defn insert-user
  [user]
  (first (db/insert! db :users user)))

(defn get-user-by-id
  [id]
  (first (db/query db
                   ["select * from users where id = ?" id])))

(defn update-user
  [user]
  (db/update! db :users
              (assoc user :modified_on (java.util.Date.))
              ["id = ?" (:id user)]))

(defn delete-user
  [user]
  (db/delete! db :users ["id = ?" (:id user)]))

(defn insert-real-estate
  [real-estate]
  (db/with-db-transaction [db db]
                          (let [id (:id (first (db/insert! db :real_estates
                                                           (dissoc real-estate :advertiser :pictures))))]
                          (doseq [picture (:pictures real-estate)]
                            (db/insert! db  :real_estates_images {:url (str "https://img.halooglasi.com" picture) :real_estate_id id})))))