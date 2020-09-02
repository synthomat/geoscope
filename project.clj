(defproject geoscope "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [slingshot "0.12.2"]
                 [ring/ring-json "0.5.0"]
                 ;; https://mvnrepository.com/artifact/net.postgis/postgis-jdbc
                 [net.postgis/postgis-jdbc "2.5.0"]

                 [cheshire "5.10.0"]
                 ;; https://mvnrepository.com/artifact/org.postgresql/postgresql
                 [org.postgresql/postgresql "42.2.14"]
                 [migratus "1.2.8"]
                 [hiccup "1.0.5"]
                 [seancorfield/next.jdbc "1.0.478"]
                 [com.cemerick/friend "0.2.3"]
                 [crypto-password "0.2.1"]
                 [aero "1.1.6"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler geoscope.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
