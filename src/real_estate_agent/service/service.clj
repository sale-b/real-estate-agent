(ns real-estate-agent.service.service
  (:require [real-estate-agent.db.dao :as dao]
            [real-estate-agent.util.cast :refer [string-to-long]])
  (:use ring.util.response))

(defn get-user
  [id]
  (let [u (dao/get-user (string-to-long id))]
    (if (empty? u)
      (bad-request "User not found!")
      (response u))
    ))