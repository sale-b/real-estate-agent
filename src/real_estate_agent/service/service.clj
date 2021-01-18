(ns real-estate-agent.service.service
  (:require [real-estate-agent.db.dao :as dao]
            [real-estate-agent.util.cast :as cast]
            [environ.core :refer [env]]
            [crypto.password.scrypt :as password]
            [cljts.geom :as geom]
            [cljts.prep :as prep]
            [cljts.relation :as relation]
            )
  (:use ring.util.response))

(def random-uuid #(.toString (java.util.UUID/randomUUID)))

(defn authorized? [token]
  (= 1 (first (dao/update-session-duration-by-id
                (:id (dao/get-unexpired-session token (cast/string-to-int (:session-allowed-inactivity-seconds env))))))))

(defn authorized-identity? [token id]
  (let [session (dao/get-unexpired-session token (cast/string-to-int (:session-allowed-inactivity-seconds env)))]
    (if (= (cast/string-to-int id) (:user_id session))
      (= 1 (first (dao/update-session-duration-by-id (:id session)))))))

(defn get-user
  [id request-headers]
  (if (nil? request-headers)
    (bad-request "Not authorized!")
    (if (authorized-identity? (request-headers "x-auth-token") id)
      (let [u (dao/get-user-by-id (cast/string-to-long id))]
        (if (empty? u)
          (bad-request "User not found!")
          (response {:id (:id u) :email (:email u)})))
      (bad-request "Not authorized!"))))

(defn register
  [user]
  (let [db-user (dao/get-user-by-email (:email user))]
    (if (nil? db-user)
      (let [token (random-uuid) db-user (dao/insert-user (assoc user :enabled true))]
        (dao/insert-session {:user_id (:id db-user) :token token})
        (header (response (dissoc db-user :password)) "x-auth-token" token))
      (bad-request "E-mail is already registered!")))
  )


(defn login
  [user]
  (let [db-user (dao/get-user-by-email (:email user))]
    (if (nil? db-user)
      (bad-request "Incorrect username or password!")
      (if (password/check (:password user) (:password db-user))
        (if (:enabled db-user)
          (let [token (random-uuid)]
            (dao/insert-session {:user_id (:id db-user) :token token})
            (header (response (dissoc db-user :password)) "x-auth-token" token))
          (bad-request "User is blocked!"))
        (bad-request "Incorrect username or password!"))
      )))

(defn make-short-description
  [description]
  (if (< 100 (count description))
    (str (subs description 0 101) "...")
    description))

(defn get-ad-by-id
  [id]
  (let [ad (dao/get-real-estate-by-id (cast/string-to-long id))]
    (if (nil? ad)
      (bad-request "Ad is not found")
      (response ad))))

(defn get-ads-paged
  [request]
  (if (> (:page request) 0)
    (response (let [db-page (vec (dao/get-paged-real-estates (:filters request) (- (:page request) 1)))]
                {:ads        (vec (map #(update-in % [:description] make-short-description) db-page))
                 :pagination {:currentPage (:page request)
                              :totalPages  (int (:total_pages (dao/get-total-pages-number)))}
                 }))
    (bad-request "Page number must be higher than zero.")))

(defn is-geolocation-satisfied?
  [pol point]
  (let [prepared-polygon (prep/prepare (geom/polygon (geom/linear-ring (into [] (map #(into () %) pol))) nil))]
    (relation/contains? prepared-polygon point)))

(defn get-all-form-data
  []
  (response {:locations       (into [] (map #(:location %) (dao/get-all-locations)))
             :microLocations  (into [] (map #(:micro_location %) (dao/get-all-micro-locations)))
             :adTypes         (into [] (map #(:ad_type %) (dao/get-all-ad-types)))
             :realEstateTypes (into [] (map #(:type %) (dao/get-all-real-estate-types)))
             :heatingTypes    (into [] (map #(:heating_type %) (dao/get-all-heating-types)))
             :floors          (into [] (map #(:floor %) (dao/get-all-floors)))
             :furnitureTypes  (into [] (map #(:furniture %) (dao/get-all-furniture-types)))
             }))

