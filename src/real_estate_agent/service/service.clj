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


;(defn is-geolocation-satisfied?
;  [id]
;  (def prepared-polygon (prepare (polygon (linear-ring [(c 20 40) (c 20 46) (c 34 56) (c 20 40)]) nil)))
;  (contains? prepared-polygon point))

