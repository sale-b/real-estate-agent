(ns real-estate-agent.rest-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [test-helpers :refer [with-database-reset]]
            [real-estate-agent.rest.rest :refer [app]]))

(use-fixtures :each with-database-reset)

(deftest test-get-url-route
  (testing "should respond with a 400 if the specified user does not exist"
    (let [req {:request-method :get
               :uri "/user/5"}
          res (app req)]
      (is (= (:status res)
             400))))
  (testing "should respond with the user if it exists."
    (let [req {:request-method :get
               :uri "/user/1"}
          res (app req)
          body (json/read-str (:body res)
                              :key-fn keyword)]
      (print body)
      (is (= {:id 1,
              :username "admin",
              :password "admin",
              :email "admin@admin.com",
              :enabled true,
              :created_on "2020-12-28T15:09:16Z",
              :modified_on "2020-12-28T15:09:16Z"}
             body)))))