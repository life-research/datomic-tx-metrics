(ns datomic-tx-metrics.core
  (:require
    [aleph.http :as http]
    [bidi.ring :as bidi]
    [clojure.string :as string]
    [environ.core :refer [env]]
    [prometheus.alpha :as prom]
    [taoensso.timbre :as log])
  (:import [io.prometheus.client CollectorRegistry]
           [io.prometheus.client.hotspot StandardExports MemoryPoolsExports
                                         GarbageCollectorExports ThreadExports
                                         ClassLoadingExports VersionInfoExports]))


;; ---- Metrics ----------------------------------------------------------------

(prom/defgauge alarms
  "Number of alarms/problems that have occurred distinguished by their kind."
  {:namespace "datomic"}
  "kind")

(prom/defgauge available-ram-megabytes
  "Unused RAM on transactor in MB."
  {:namespace "datomic"})

(prom/defgauge object-cache-size
  "Number of segments in the Datomic object cache."
  {:namespace "datomic"})

(prom/defgauge remote-peers
  "Number of remote peers connected."
  {:namespace "datomic"})

(prom/defgauge successful-metric-reports
  "Number of successful metric reports over a 1 min period."
  {:namespace "datomic"})

(prom/defcounter transacted-datoms-total
  "Number of transacted datoms."
  {:namespace "datomic"})

(prom/defcounter transactions-total
  "Total number of transactions."
  {:namespace "datomic"})

(prom/defgauge transactions-batch
  "Number of transactions batched into a single write to the log."
  {:namespace "datomic"})

(prom/defcounter transacted-bytes-total
  "Total volume of transaction data to log, peers in bytes."
  {:namespace "datomic"})

(prom/defcounter transactions-msec-total
  "Total time of transactions in msec."
  {:namespace "datomic"})

(prom/defcounter transactions-add-fulltext-msec-total
  "Total time of transactions spent to add fulltext."
  {:namespace "datomic"})

(prom/defcounter transactions-write-log-msec-total
  "Total time of transactions spent writing to log per transaction batch."
  {:namespace "datomic"})

(prom/defgauge datoms
  "Number of unique datoms in the index."
  {:namespace "datomic"})

(prom/defgauge index-datoms
  "Number of datoms stored by the index, all sorts."
  {:namespace "datomic"})

(prom/defgauge index-segments
  "Number of segments in the index."
  {:namespace "datomic"})

(prom/defgauge index-writes
  "Number of segments written by indexing job, reported at end."
  {:namespace "datomic"})

(prom/defgauge index-writes-msec
  "Time per index segment write."
  {:namespace "datomic"})

(prom/defgauge index-creation-msec
  "Time to create index in msec, reported at end of indexing job."
  {:namespace "datomic"})

(prom/defgauge index-fulltext-creation-msec
  "Time to create fulltext portion of index in msec."
  {:namespace "datomic"})

(prom/defgauge memory-index-consumed-megabytes
  "RAM consumed by memory index in MB."
  {:namespace "datomic"})

(prom/defgauge memory-index-fill-msec
  "Estimate of the time to fill the memory index, given the current write load."
  {:namespace "datomic"})

(prom/defcounter storage-write-operations-total
  "Total number of storage write operations."
  {:namespace "datomic"})

(prom/defcounter storage-write-bytes-total
  "Total number of bytes written to storage."
  {:namespace "datomic"})

(prom/defgauge storage-write-msec
  "Time spent writing to storage."
  {:namespace "datomic"})

(prom/defcounter storage-read-operations-total
  "Total number of storage read operations."
  {:namespace "datomic"})

(prom/defcounter storage-read-bytes-total
  "Total number of bytes read from storage."
  {:namespace "datomic"})

(prom/defgauge storage-read-msec
  "Time spent reading from storage."
  {:namespace "datomic"})

(prom/defgauge storage-backoff-msec
  "Time spent in backoff/retry around calls to storage."
  {:namespace "datomic"})

(prom/defcounter storage-backoff-retries-total
  "Total number of retried storage operations."
  {:namespace "datomic"})

(prom/defgauge object-cache-hits-ratio
  "Datomic object cache hit ratio."
  {:namespace "datomic"})

(prom/defgauge garbage-segments
  "Number of garbage segments created."
  {:namespace "datomic"})

(prom/defgauge heartbeats-msec
  "Time spent writing to storage as part of the heartbeat (transactor writes location)."
  {:namespace "datomic"})

(prom/defgauge heartbeats
  "Number of heartbeats."
  {:namespace "datomic"})

(def ^:private metrics-registry
  (doto (CollectorRegistry. true)
    (.register (StandardExports.))
    (.register (MemoryPoolsExports.))
    (.register (GarbageCollectorExports.))
    (.register (ThreadExports.))
    (.register (ClassLoadingExports.))
    (.register (VersionInfoExports.))
    (.register alarms)
    (.register available-ram-megabytes)
    (.register object-cache-size)
    (.register remote-peers)
    (.register successful-metric-reports)
    (.register transacted-datoms-total)
    (.register transactions-total)
    (.register transactions-batch)
    (.register transacted-bytes-total)
    (.register transactions-msec-total)
    (.register transactions-add-fulltext-msec-total)
    (.register transactions-write-log-msec-total)
    (.register datoms)
    (.register index-datoms)
    (.register index-segments)
    (.register index-writes)
    (.register index-writes-msec)
    (.register index-creation-msec)
    (.register index-fulltext-creation-msec)
    (.register memory-index-consumed-megabytes)
    (.register storage-write-operations-total)
    (.register storage-write-bytes-total)
    (.register storage-write-msec)
    (.register storage-read-operations-total)
    (.register storage-read-bytes-total)
    (.register storage-read-msec)
    (.register storage-backoff-msec)
    (.register storage-backoff-retries-total)
    (.register object-cache-hits-ratio)
    (.register garbage-segments)
    (.register heartbeats-msec)
    (.register heartbeats)
    ))


;; ---- Callback ---------------------------------------------------------------

(defn tx-metrics-callback-handler
  "Called by Datomic transactor transferring its metrics."
  [tx-metrics]
  (if-let [{:keys [sum]} (:AlarmIndexingJobFailed tx-metrics)]
    (prom/set! alarms "index-job-failed" sum)
    (prom/set! alarms "index-job-failed" 0))

  (when-let [{:keys [sum]} (:AlarmBackPressure tx-metrics)]
    (prom/set! alarms "back-pressure" sum)
    (prom/set! alarms "back-pressure" 0))

  (when-let [{:keys [sum]} (:AlarmUnhandledException tx-metrics)]
    (prom/set! alarms "unhandled-exception" sum)
    (prom/set! alarms "unhandled-exception" 0))

  (->> (keys tx-metrics)
       (filter
         (fn [key]
           (and (string/starts-with? (name key) "Alarm")
                (not= key :Alarm)
                (not= key :AlarmIndexingJobFailed)
                (not= key :AlarmBackPressure)
                (not= key :AlarmUnhandledException))))
       (reduce
         (fn [count {:keys [sum]}]
           (+ count sum))
         0)
       (prom/set! alarms "other"))

  (when-let [mb (:AvailableMB tx-metrics)]
    (prom/set! available-ram-megabytes mb))

  (when-let [size (:ObjectCacheCount tx-metrics)]
    (prom/set! object-cache-size size))

  (when-let [{:keys [sum]} (:RemotePeers tx-metrics)]
    (prom/set! remote-peers sum))

  (when-let [{:keys [sum]} (:MetricsReport tx-metrics)]
    (prom/set! successful-metric-reports sum))

  (when-let [{:keys [sum count]} (:TransactionDatoms tx-metrics)]
    (prom/inc! transacted-datoms-total sum)
    (prom/inc! transactions-total count))

  (if-let [{:keys [count]} (:TransactionBatch tx-metrics)]
    (prom/set! transactions-batch count)
    (prom/clear! transactions-batch))

  (when-let [{:keys [sum]} (:TransactionBytes tx-metrics)]
    (prom/inc! transacted-bytes-total sum))

  (when-let [{:keys [sum]} (:TransactionMsec tx-metrics)]
    (prom/inc! transactions-msec-total sum))

  (when-let [{:keys [sum]} (:DbAddFulltextMsec tx-metrics)]
    (prom/inc! transactions-add-fulltext-msec-total sum))

  (when-let [{:keys [sum]} (:LogWriteMsec tx-metrics)]
    (prom/inc! transactions-write-log-msec-total sum))

  (if-let [{:keys [sum]} (:Datoms tx-metrics)]
    (prom/set! datoms sum)
    (prom/clear! datoms))

  ; TODO: check if resetting this is actually what resembles the transactor state
  (if-let [{:keys [sum]} (:IndexDatoms tx-metrics)]
    (prom/set! index-datoms sum)
    (prom/clear! index-datoms))

  ; TODO: check if resetting this is actually what resembles the transactor state
  (if-let [{:keys [sum]} (:IndexSegments tx-metrics)]
    (prom/set! index-segments sum)
    (prom/clear! index-segments))

  (if-let [{:keys [sum]} (:IndexWrites tx-metrics)]
    (prom/set! index-writes sum)
    (prom/clear! index-writes))

  (if-let [{:keys [sum]} (:IndexWriteMsec tx-metrics)]
    (prom/set! index-writes-msec sum)
    (prom/clear! index-writes-msec))

  (if-let [{:keys [sum]} (:CreateEntireIndexMsec tx-metrics)]
    (prom/set! index-creation-msec sum)
    (prom/clear! index-creation-msec))

  (if-let [{:keys [sum]} (:CreateFulltextIndexMsec tx-metrics)]
    (prom/set! index-fulltext-creation-msec sum)
    (prom/clear! index-fulltext-creation-msec))

  (when-let [{:keys [sum]} (:MemoryIndexMB tx-metrics)]
    (prom/set! memory-index-consumed-megabytes sum))

  (if-let [{:keys [sum]} (:MemoryIndexFillMsec tx-metrics)]
    (prom/set! memory-index-fill-msec sum)
    (prom/clear! memory-index-fill-msec))

  (when-let [{:keys [sum count]} (:StoragePutBytes tx-metrics)]
    (prom/inc! storage-write-operations-total count)
    (prom/inc! storage-write-bytes-total sum))

  (if-let [{:keys [sum]} (:StoragePutMsec tx-metrics)]
    (prom/set! storage-write-msec sum)
    (prom/clear! storage-write-msec))

  (when-let [{:keys [sum count]} (:StorageGetBytes tx-metrics)]
    (prom/inc! storage-read-operations-total count)
    (prom/inc! storage-read-bytes-total sum))

  (if-let [{:keys [sum]} (:StorageGetMsec tx-metrics)]
    (prom/set! storage-read-msec sum)
    (prom/clear! storage-read-msec))

  (if-let [{:keys [sum count]} (:StorageBackoff tx-metrics)]
    (do
      (prom/set! storage-backoff-msec sum)
      (prom/inc! storage-backoff-retries-total count))
    (prom/clear! storage-backoff-msec))

  ; TODO: discuss if this is actually a sane move - resetting this might be misleading without another metrics showing how many cache requests were made...
  (if-let [{:keys [sum count]} (:ObjectCache tx-metrics)]
    (prom/set! object-cache-hits-ratio (/ (double sum) count))
    (prom/clear! object-cache-hits-ratio))

  (if-let [{:keys [sum]} (:GarbageSegments tx-metrics)]
    (prom/set! garbage-segments sum)
    (prom/clear! garbage-segments))

  (when-let [{:keys [sum count]} (:HeartbeatMsec tx-metrics)]
    (prom/set! heartbeats-msec sum)
    (prom/set! heartbeats count))
  )

;; ---- Server -----------------------------------------------------------------

(defn- wrap-not-found
  "Middleware which returns a 404 response if no downstream handler can be
   found processing a request. Otherwise forwards the request to the found
   handler as well as its response to the caller."
  [handler]
  (fn [req]
    (if-let [resp (handler req)]
      resp
      {:status 404
       :header {:content-type "text/plain"}
       :body "Not Found"})))


(defn- health-handler
  "Health handler returning a 200 response code with 'OK' as a response body."
  [_]
  {:status 200 :body "OK"})


(defn- metrics-handler
  "Metrics handler returning the transactor and JVM metrics of a transactor."
  [_]
  (prom/dump-metrics metrics-registry))


(def ^:private routes
  "Defines the routes for the web server."
  ["/"
   [["health" {:get health-handler}]
    ["metrics" {:get metrics-handler}]]])


(defn- routing
  "Creates a ring handler for routing requests to the appropriate sub-handler
   based on `routes`."
  [routes]
  (-> (bidi/make-handler routes)
      (wrap-not-found)))


(defn- start-metrics-server
  "Starts the web server that can be used to scrape transactor + JVM metrics."
  []
  (let [metrics-port (Integer/parseInt (or (:metrics-port env) "11509"))]
    (log/info "Starting metrics server on port " metrics-port)
    (http/start-server (routing routes) {:port metrics-port})))


(start-metrics-server)
