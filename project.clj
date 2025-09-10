
(defproject clojurenew "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [javax.xml.bind/jaxb-api "2.3.1"]
                 [http-kit "2.2.0"]
                 [org.ocpsoft.prettytime/prettytime "3.2.7.Final"]
                 [clj-time "0.14.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [metosin/jsonista "0.3.8"]
                 [ring/ring-json "0.5.1"]
                 [cheshire "5.11.0"]
                 [org.xerial/sqlite-jdbc "3.15.1"]
                 [lein-ancient "1.0.0-RC3"]
                 [cheshire "5.12.0"]
                 [ring/ring-codec "1.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-anti-forgery "1.4.0"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-defaults "0.3.4"]
                 [clojure-interop/javax.imageio "1.0.5"]
                 [clojure-interop/java.security "1.0.5"]
                 [aleph "0.4.7"]
                 [clojure-interop/java.util "1.0.5"]
                 [org.clojure/data.json "2.5.1"]
                 [hiccup "2.0.0"]
                 [gorillalabs/ring-anti-forgery-strategies "1.2.0"]
                 [compojure "1.7.1" :exclusions [ring/ring-core]]]
  :repl-options {:init-ns clojurenew.core}
  :main clojurenew.core)

