(defproject datomic-tx-metrics "0.1.0"
  :description "Containing a callback handler for collecting Datomic Transactor + JVM metrics for consumption (e.g. by Prometheus) using a web endpoint offered by the included web server."
  :dependencies
  [[aleph "0.4.6"]
   [bidi "2.1.6"]
   [com.taoensso/timbre "4.10.0"]
   [environ "1.1.0"]
   [io.prometheus/simpleclient_hotspot "0.5.0"]
   [org.clojure/clojure "1.10.1"]
   [prom-metrics "0.5-alpha2"]
   [ring/ring-core "1.7.1"]])
