(defproject geoscope "0.3.1"
  :description "GPS tracking data receiver for the Overland app"
  :url "https://github.com/synthomat/geoscope"
  :min-lein-version "2.0.0"
  :repositories [["osgeo" "https://repo.osgeo.org/repository/release/"]
                 ["maven" "https://repo1.maven.org/maven2/"]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 ;; https://mvnrepository.com/artifact/net.postgis/postgis-jdbc
                 [net.postgis/postgis-jdbc "2.5.0"]
                 ;; https://mvnrepository.com/artifact/org.orbisgis/h2gis
                 ;[org.orbisgis/h2gis "1.5.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 ;; https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
                 [ch.qos.logback/logback-classic "1.2.3"]

                 [seancorfield/next.jdbc "1.1.610"]
                 [migratus "1.2.8"]
                 [slingshot "0.12.2"]
                 [buddy "2.0.0"]
                 [midje "1.9.9"]
                 [cheshire "5.10.0"]
                 ;; https://mvnrepository.com/artifact/org.postgresql/postgresql
                 [org.postgresql/postgresql "42.2.14"]
                 [hiccup "1.0.5"]
                 ;[crypto-password "0.2.1"]
                 [aero "1.1.6"]
                 [org.clojure/data.xml "0.0.8"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler geoscope.handler/app
         :init    geoscope.handler/init-app!}
  :main geoscope.main
  :uberjar-name "geoscope-standalone.jar"
  :profiles

  {:uberjar {:aot :all}
   :dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                            [ring/ring-mock "0.3.2"]]
             :plugins [[lein-midje "3.2.1"]]}})
