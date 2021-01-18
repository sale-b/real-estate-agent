(ns real-estate-agent.db.dao
  (:import [java.sql PreparedStatement])
  (:require [clojure.java.jdbc :as db]
            [environ.core :refer [env]]
            [clj-time.coerce :as c]
            [crypto.password.scrypt :as password])
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

(def page-size 10)

(defn valid? [v] (if (not (nil? v))
                   (if (number? v)
                     (> v 0)
                     (not (empty? v)))
                   false))

(defn insert-user
  [user]
  (first (db/insert! db :users (assoc user :password (password/encrypt (:password user))))))

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


(defn prepare-arguments
  [filter page-number]
  (let [valid-filter [(:priceHigher filter)
                      (:priceLes filter)
                      (:spaceAreaHigher filter)
                      (:spaceAreaLes filter)
                      (:roomsNumberHigher filter)
                      (:roomsNumberLes filter)
                      (:type filter)
                      (:adType filter)
                      (:heatingType filter)
                      (:floor filter)
                      (:furniture filter)
                      (:location filter)
                      (:microLocation filter)
                      ]] (concat
                           (if (not (nil? filter)) (keep #(if (valid? %) %)
                                                         (flatten valid-filter))
                                                   '())
                           (repeat 1 page-size)
                           (repeat 1 (* page-size page-number)))))

(defn prepare-query
  [filter]
  (str "select  distinct on (id) re.id, re.tittle, re.phone, re.location, re.micro_location, re.price, re.\"type\", re.ad_type, re.rooms_number, re.floor, re.description, re.living_space_area, re.furniture, re.heating_type, re.created_on, rei.url as img_url "
       "from real_estates re "
       "left join real_estates_images  rei "
       "on   re.id = "
       "( "
       "select rei.real_estate_id from real_estates_images where real_estate_id = re.id order by id limit 1 "
       ") "
       "where 1 = 1 "
       (if (and (not (nil? filter)) (valid? (:priceHigher filter)))
         "and price >= ? "
         "")
       (if (and (not (nil? filter)) (valid? (:priceLes filter)))
         "and price <= ? "
         "")
       (if (and (not (nil? filter)) (valid? (:spaceAreaHigher filter)))
         "and living_space_area >= ? "
         "")
       (if (and (not (nil? filter)) (valid? (:spaceAreaLes filter)))
         "and living_space_area <= ? "
         "")
       (if (and (not (nil? filter)) (valid? (:roomsNumberHigher filter)))
         "and rooms_number >= ? "
         "")
       (if (and (not (nil? filter)) (valid? (:roomsNumberLes filter)))
         "and rooms_number <= ? "
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:type filter))) 0))
         (str "and type in ("
              (clojure.string/join ", " (take (count (remove nil? (:type filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:adType filter))) 0))
         (str "and ad_type in ("
              (clojure.string/join ", " (take (count (remove nil? (:adType filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:heatingType filter))) 0))
         (str "and heating_type in ("
              (clojure.string/join ", " (take (count (remove nil? (:heatingType filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:floor filter))) 0))
         (str "and floor in ("
              (clojure.string/join ", " (take (count (remove nil? (:floor filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:furniture filter))) 0))
         (str "and furniture in ("
              (clojure.string/join ", " (take (count (remove nil? (:furniture filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:location filter))) 0))
         (str "and location in ("
              (clojure.string/join ", " (take (count (remove nil? (:location filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (> (count (remove nil? (:microLocation filter))) 0))
         (str "and micro_location in ("
              (clojure.string/join ", " (take (count (remove nil? (:microLocation filter))) (repeat "?")))
              ") ")
         "")
       (if (and (not (nil? filter)) (:hasPictures filter))
         "and has_pictures = true "
         "")
       "order by re.id desc "
       "limit ? "                                           ;ads per page
       "offset ?"))


(defn get-paged-real-estates
  [filter page-number]
  (db/query db
            (into [] (concat [(prepare-query filter)] (prepare-arguments filter page-number)))))

(defn get-total-pages-number
  []
  (first (db/query db ["select greatest(ceiling(count(id) / ?), 1) as total_pages from real_estates" page-size])))

(defn get-all-locations
  []
  (db/query db
            ["select distinct \"location\" from real_estates order by \"location\" asc"]))

(defn get-all-micro-locations
  []
  (db/query db
            ["select distinct micro_location from real_estates order by micro_location asc"]))

(defn get-all-real-estate-types
  []
  (db/query db
            ["select distinct type from real_estates order by type asc"]))

(defn get-all-furniture-types
  []
  (db/query db
            ["select distinct furniture from real_estates where furniture is not null order by furniture asc"]))

(defn get-all-ad-types
  []
  (db/query db
            ["select distinct ad_type from real_estates order by ad_type asc"]))

(defn get-all-heating-types
  []
  (db/query db
            ["select distinct heating_type from real_estates where heating_type is not null order by heating_type asc"]))

(defn get-all-floors
  []
  (db/query db
            ["select distinct floor from real_estates where floor is not null order by floor asc"]))

(defn get-last-inserted-real-estate
  []
  (first (db/query db
                   ["select * from real_estates order by created_on desc limit 1"])))




