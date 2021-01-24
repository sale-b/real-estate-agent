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
  (first (db/delete! db :users ["id = ?" (:id user)])))

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
  (str
    "from real_estates ad "
    "left join (select min (rei.id) \"img_id\", rei.real_estate_id "
    "from real_estates_images rei group by  rei.real_estate_id order by  rei.real_estate_id) B "
    "ON ad.id = B.real_estate_id left join real_estates_images img  ON img.id = B.img_id "
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
    "order by ad.id desc "
    "limit ? "                                              ;ads per page
    "offset ?"))


(defn get-paged-real-estates
  [filter page-number]
  (db/query db
            (into [] (concat [(str "select ad.*, img.url as img_url " (prepare-query filter))] (prepare-arguments filter page-number)))))

(defn get-total-pages-number
  [filter]
  (first (db/query db
                   (into [] (concat [(subs (str "select ceiling(count(ad.id)::numeric/ ?) as total_pages " (prepare-query filter)) 0 (+ (count (prepare-query filter)) 19))] (seq [page-size]) (drop-last 2 (prepare-arguments filter 0)))))))


(defn get-all-locations
  []
  (db/query db
            ["select distinct \"location\" from real_estates order by \"location\" asc"]))

(defn get-all-micro-locations
  []
  (db/query db
            ["select distinct micro_location from real_estates order by micro_location asc"]))

(defn get-all-micro-locations-for-location
  [locations]
  (db/query db
            (into [] (concat [(str "select distinct micro_location from real_estates where \"location\" in ( "
                                   (clojure.string/join ", " (take (count (remove clojure.string/blank? locations)) (repeat "?")))
                                   " ) order by micro_location asc")] (flatten (remove clojure.string/blank? locations))))))


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


(defn insert-saved-filter
  [saved-filter user-id]
  (let [prepared-saved-filter
        {:locations             (str (:locations saved-filter))
         :ad_types              (str (:ad_types saved-filter))
         :floors                (str (:floors saved-filter))
         :furniture             (str (:furniture saved-filter))
         :real_estate_types     (str (:real_estate_types saved-filter))
         :micro_locations       (str (:micro_locations saved-filter))
         :geolocation           (str (:geolocation saved-filter))
         :heating_types         (str (:heating_types saved-filter))
         :max_price             (:max_price saved-filter)
         :min_price             (:min_price saved-filter)
         :max_living_space_area (:max_living_space_area saved-filter)
         :min_living_space_area (:min_living_space_area saved-filter)
         :max_rooms_number      (:max_rooms_number saved-filter)
         :min_rooms_number      (:min_rooms_number saved-filter)
         :tittle                (:tittle saved-filter)
         :has_pictures          (:has_pictures saved-filter)
         :email_subscribed      (:email_subscribed saved-filter)
         :user_id               user-id
         }]
    ;(println prepared-saved-filter)
    (first (db/insert! db :saved_filters (assoc prepared-saved-filter :user_id user-id)))))


(defn delete-saved-filter
  [id]
  (first (db/delete! db :saved_filters ["id = ?" id])))

(defn get-saved-filter-by-id
  [id]
  (let [saved-filter (first (db/query db
                                      ["select * from saved_filters where id = ?" id]))]

    {:locations             (if (empty? (:locations saved-filter)) nil (read-string (:locations saved-filter)))
     :ad_types              (if (empty? (:ad_types saved-filter)) nil (read-string (:ad_types saved-filter)))
     :floors                (if (empty? (:floors saved-filter)) nil (read-string (:floors saved-filter)))
     :furniture             (if (empty? (:furniture saved-filter)) nil (read-string (:furniture saved-filter)))
     :real_estate_types     (if (empty? (:real_estate_types saved-filter)) nil (read-string (:real_estate_types saved-filter)))
     :micro_locations       (if (empty? (:micro_locations saved-filter)) nil (read-string (:micro_locations saved-filter)))
     :heating_types         (if (empty? (:heating_types saved-filter)) nil (read-string (:heating_types saved-filter)))
     :geolocation           (if (empty? (:geolocation saved-filter)) nil (read-string (:geolocation saved-filter)))
     :max_price             (:max_price saved-filter)
     :min_price             (:min_price saved-filter)
     :max_living_space_area (:max_living_space_area saved-filter)
     :min_living_space_area (:min_living_space_area saved-filter)
     :max_rooms_number      (:max_rooms_number saved-filter)
     :min_rooms_number      (:min_rooms_number saved-filter)
     :tittle                (:tittle saved-filter)
     :has_pictures          (:has_pictures saved-filter)
     :email_subscribed      (:email_subscribed saved-filter)
     :created_on            (:created_on saved-filter)
     :modified_on           (:modified_on saved-filter)
     :user_id               id
     }))

(defn get-saved-filter-by-user-id
  [user-id]
  (db/query db
            ["select id, tittle, email_subscribed from saved_filters where user_id = ?" user-id]))

(defn count-users-saved-filters
  [user-id]
  (:count (first (db/query db
                           ["select count(id) from saved_filters where user_id = ?" user-id]))))




