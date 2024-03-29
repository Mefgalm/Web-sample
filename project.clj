(defproject web-sample "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.500"]
                 [honeysql "0.9.4"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.postgresql/postgresql "42.1.4"]                 
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.match "0.3.0"]]
  :main web-sample.core
  :repl-options {:init-ns web-sample.core})