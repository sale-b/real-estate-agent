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

(defn get-user-by-email
  [email]
  (first (db/query db
                   ["select * from users where email = ?" email])))

(defn insert-session
  [session]
  (first (db/insert! db :persistent_logins session)))

(defn get-unexpired-session
  [token token-duration]
  (first (db/query db
                   ["select *
                   from persistent_logins
                   where token = ?
                   and (SELECT EXTRACT(EPOCH FROM (current_timestamp - last_used)) < ?)" token token-duration])))

(defn update-session-duration-by-id
  [id]
  (db/update! db :persistent_logins
              {:last_used (java.util.Date.)}
              ["id = ?" id]))

(defn get-user-by-email-and-pass
  [email pass]
  (first (db/query db
                   ["select * from users where email = ? and password = ?" email pass])))


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
                          (let [db-real-estate (first (db/insert! db :real_estates
                                                                  (dissoc real-estate :advertiser :pictures)))]
                            (assoc db-real-estate :pictures
                                                  (into [] (map :url (db/insert-multi! db :real_estates_images (for [picture (:pictures real-estate)]
                                                                                                                 {:url            (str "https://img.halooglasi.com" picture)
                                                                                                                  :real_estate_id (:id db-real-estate)}))))))))


(defn get-real-estate-by-id
  [id]
  (assoc (first (db/query db
                          ["select * from real_estates where id = ?" id]))
    :pictures (into [] (map :url (db/query db
                                           ["select * from real_estates_images where real_estate_id = ?" id])))))

(defn get-last-inserted-real-estate
  []
  (first (db/query db
                   ["select * from real_estates ORDER BY created_on DESC LIMIT 1"])))

