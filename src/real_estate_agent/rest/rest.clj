(ns real-estate-agent.rest.rest
  (:require [real-estate-agent.service.service :as service]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:use compojure.core
        ring.adapter.jetty
        ring.middleware.json
        ring.util.response))

(defn cors-handler [my-routes]
  (wrap-cors my-routes
             :access-control-allow-origin [#"http://localhost:8080"]
             :access-control-allow-methods [:get :put :post :delete :options]
             :access-control-allow-headers #{"accept"
                                             "content-type"
                                             "x-auth-token"
                                             "user-id"
                                             }
             :access-control-expose-headers #{"x-auth-token"}))

(defroutes my-routes
           (POST "/register" [] (fn [req] (service/register (:body req))))
           (POST "/login" [] (fn [req] (service/login (:body req))))
           (GET "/user/:id" [id] (fn [req] (service/get-user id (:headers req))))
           (GET "/ad/:id" [id] (service/get-ad-by-id id))
           (POST "/page" [] (fn [req] (service/get-ads-paged (:body req))))
           (GET "/get-form-props" [] (service/get-all-form-data))
           (POST "/refresh-micro-locations" [] (fn [req] (service/get-micro-locations-for-location (:body req))))
           (POST "/save-filters" [] (fn [req] (service/save-filters (:headers req) (:body req))))
           (GET "/saved-filter/:id" [id] (fn [req] (service/get-saved-filter-by-id id (:headers req))))
           (GET "/saved-filters/:user-id" [user-id] (fn [req] (service/get-saved-filters-by-user-id user-id (:headers req))))
           (GET "/delete-saved-filter/:id" [id] (fn [req] (service/delete-saved-filter-by-id id (:headers req))))
           (route/resources "/"))

(def app (wrap-json-response (wrap-json-body (cors-handler my-routes) {:keywords? true})))