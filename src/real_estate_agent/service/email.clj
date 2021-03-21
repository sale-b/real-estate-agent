(ns real-estate-agent.service.email
  (:require [postal.core :refer [send-message]]
            [environ.core :refer [env]]))

(def email (:email-username env))
(def pass (:email-pass env))

(def conn {:host "smtp.gmail.com"
           :ssl  true
           :user email
           :pass pass})

(send-message conn {:from    email
                    :to      email
                    :subject "A message, from the past"
                    :body    [:alternative
                              {:type    "text/plain"
                               :content ""}
                              {:type    "text/html; charset=utf-8"
                               :content "<html><body><h3>Pronađenisunovioglasi!</h3><table border='1' style='border-collapse: collapse;border: 1px solid #ddd;'><tr style='background-color: #2eb8b8;color: white;'><th>Naziv</th><th>Cena</th><th>Povrsina</th><th>Lokacija</th><th>URL</th></tr><tr><td>Nazivoglasa</td><td>100E</td><td>50m2</td><td>Centarbeograda</td><td>https://halooglasi.com/oglas/id/2216545</td></tr></table></body></html>"}]})