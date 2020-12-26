(ns real-estate-agent.service.service
  (:require [real-estate-agent.db.dao :as dao])
  (:use ring.util.response))

(defn get-user
  [id]
  (let [u (dao/get-user id)]
    (if (empty? u)
      (bad-request "User not found!")
      (response u))
    ))