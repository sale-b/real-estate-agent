(ns real-estate-agent.service.service
  (:require [real-estate-agent.db.dao :as dao]
            [real-estate-agent.util.cast :as cast]
            [environ.core :refer [env]]
            [cljts.geom :as geom]
            [cljts.prep :as prep]
            [cljts.relation :as relation]
            )
  (:use ring.util.response))

(def random-uuid #(.toString (java.util.UUID/randomUUID)))

(defn authorized? [token]
  (= 1 (first (dao/update-session-duration-by-id
                (:id (dao/get-unexpired-session token (cast/string-to-int (:session-allowed-inactivity-seconds env))))))))

(defn get-user
  [id request-headers]
  (if (nil? request-headers)
    (bad-request "Not autorized!")
    (if (authorized? (request-headers "x-auth-token"))
      (let [u (dao/get-user-by-id (cast/string-to-long id))]
        (if (empty? u)
          (bad-request "User not found!")
          (response u)))
      (bad-request "Not autorized!"))))

(defn register
  [user]
  (let [db-user (dao/get-user-by-email (:email user))]
    (if (nil? db-user)
      (response (dao/insert-user (assoc user :enabled true)))
      (bad-request "E-mail is already registered!")))
  )


(defn login
  [user]
  (let [db-user (dao/get-user-by-email-and-pass (:email user) (:password user))]
    (if (nil? db-user)
      (bad-request "Incorrect username or password!")
      (if (= true (:enabled db-user))
        (let [token (random-uuid)]
          (dao/insert-session {:email (:email db-user) :token token})
          (header (response db-user) "x-auth-token" token))
        (bad-request "User is blocked!"))
      )))

;(defn is-geolocation-satisfied?
;  [id]
;  (def prepared-polygon (prepare (polygon (linear-ring [(c 20 40) (c 20 46) (c 34 56) (c 20 40)]) nil)))
;  (contains? prepared-polygon point))

