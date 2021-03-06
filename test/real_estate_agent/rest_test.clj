(ns real-estate-agent.rest-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [test-helpers :refer [with-database-reset]]
            [real-estate-agent.rest.rest :refer [app]]))

(use-fixtures :each with-database-reset)

(deftest test-get-url-route
  (testing "should respond with a 400 if the specified user does not exist"
    (let [req {:request-method :get
               :uri            "/user/5"
               :headers        {"x-auth-token" "334513c6-139c-4393-b91e-03224461e38d"}
               }
          res (app req)]
      (is (= (:status res)
             400))))
  (testing "should respond with the user if it exists."
    (let [req {:request-method :get
               :uri            "/user/1"
               :headers        {"x-auth-token" "334513c6-139c-4393-b91e-03224461e38d"}
               }
          res (app req)
          body (json/read-str (:body res)
                              :key-fn keyword)]
      (is (= {:id          1
              :email       "admin@admin.com"}
             body))
      ))
  (testing "should respond with the status 400 if not authorized."
    (let [req {:request-method :get
               :uri            "/user/1"}
          res (app req)]
      (is (= (:status res) 400))
      )))