(defproject real-estate-agent "0.1.0-SNAPSHOT"
  :description "A real estate agent is a web application that helps clients buy or rent real estate by extracting and collecting ads from multiple sources, without intermediaries and provides a search and notification based system."
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.18"]
                 [ragtime "0.8.0"]
                 [environ "1.2.0"]
                 [enlive "1.1.6"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-json "0.5.0"]
                 [ring-cors "0.1.13"]
                 [ring/ring-defaults "0.3.2"]
                 [metosin/ring-http-response "0.9.1"]
                 [compojure "1.6.2"]
                 [overtone/at-at "1.2.0"]]

  :plugins [[lein-environ "1.2.0"]
            [lein-ancient "0.6.15"]]

  :source-paths ["src"]
  :test-paths ["test"]
  :target-path "target/%s"

  :profiles
  {:dev {:env {:environment "development"}
         :main real-estate-agent.main
         :aot :all
         }

   :test {:env {:environment "test"}
          :dependencies [[pjstadig/humane-test-output "0.10.0"]]
          :injections [(require 'pjstadig.humane-test-output)
                       (pjstadig.humane-test-output/activate!)]}

   :prod {:env {:environment "production"}
          :uberjar-name "app-standalone.jar"
          :main main
          :aot :all}})

