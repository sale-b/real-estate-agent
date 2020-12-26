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
                                             "custom-header"
                                             }))

(defroutes my-routes
           (GET "/user/:id" [id] (service/get-user id))
           (route/resources "/"))

(def app (wrap-json-response (wrap-json-body (cors-handler my-routes) {:keywords? true})))