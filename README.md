# datomic-tx-metrics
Collecting Datomic Transactor + JVM metrics for consumption by [Prometheus](https://prometheus.io/) by offering a web endpoint.

## How does it work?

### Registering the metrics collector at the transactor.
Add the following line to your `transactor.properties` file:

```
metrics-callback=datomic-tx-metrics.core/tx-metrics-callback-handler
```

Next ensure that the JAR (you can download one in the release section) is present within Datomic's `/lib` directory to be loaded at runtime. An example leveraging a Docker container can be found in the examples section of this repository.

### Configuring the metrics collector

* specifiy the port of the web server fired up by the metrics collector using the environment variable `METRICS_PORT` when starting the transactor (defaults to _11509_) 

### Scraping Metrics

* Metrics collector fires up a web server when loaded by the transactor
* Metrics are typically sent from the transactor to the callback function every `10s` (keep this in mind since values might not change over this duration).
* Scrape the collected metrics by requesting the started metrics web server under the `/metrics` endpoint

The following is an exemplary Prometheus configuration file for scraping the metrics endpoint:

```yaml
global:
  scrape_interval: 15s
  scrape_timeout: 10s
  evaluation_interval: 15s
alerting:
  alertmanagers:
  - static_configs:
    - targets: []
    scheme: http
    timeout: 10s
    api_version: v1
scrape_configs:
- job_name: prometheus
  honor_timestamps: true
  scrape_interval: 15s
  scrape_timeout: 10s
  metrics_path: /metrics
  scheme: http
  static_configs:
  - targets:
    - localhost:9090
- job_name: datomic-tx-metrics
  scrape_interval: 10s
  scrape_timeout: 5s
  metrics_path: /metrics
  scheme: http
  static_configs:
  - targets:
    - localhost:11509
```
_**Note:** adjust the target according to your own deployment state._
  

## What JVM metrics are covered?

The following JVM metrics are covered as defined by [Prometheus Hotspot](https://github.com/prometheus/client_java/tree/parent-0.5.0/simpleclient_hotspot/src/main/java/io/prometheus/client/hotspot):

* Standard Exports
* MemoryPoolsExports
* GarbageCollectorExports
* ThreadExports
* ClassLoadingExports
* VersionInfoExports

|Metric|Type|Labels|Unit|Description|
|------|----|------|----|-----------|
|process_cpu_seconds_total|counter||seconds|Total user and system CPU time spent in seconds.|
|process_start_time_seconds|gauge||seconds|Start time of the process since unix epoch in seconds.|
|process_open_fds|gauge|||Number of open file descriptors.|
|process_max_fds|gauge|||Maximum number of open file descriptors.|
|process_virtual_memory_bytes|gauge||bytes|Virtual memory size in bytes.|
|process_resident_memory_bytes|gauge||bytes|Resident memory size in bytes.|
|jvm_memory_bytes_used|gauge|area|bytes|Used bytes of a given JVM memory area.|
|jvm_memory_bytes_committed|gauge|area|bytes|Committed bytes of a given JVM memory area.|
|jvm_memory_bytes_max|gauge|area|bytes|Max bytes of a given JVM memory area.|
|jvm_memory_bytes_init|gauge|area|bytes|Initial bytes of a given JVM memory area.|
|jvm_memory_pool_bytes_used|gauge|pool|bytes|Used bytes of a given JVM memory pool.|
|jvm_memory_pool_bytes_committed|gauge|pool|bytes|Committed bytes of a given JVM memory pool.|
|jvm_memory_pool_bytes_max|gauge|pool|bytes|Max bytes of a given JVM memory pool.|
|jvm_memory_pool_bytes_init|gauge|pool|bytes|Initial bytes of a given JVM memory pool.|
|jvm_gc_collection_seconds_count|summary|gc|seconds|Times a given JVM garbage collector ran.|
|jvm_gc_collection_seconds_sum|summary|gc|seconds|Time spent in a given JVM garbage collector in seconds.|
|jvm_threads_current|gauge|||Current thread count of a JVM.|
|jvm_threads_daemon|gauge|||Daemon thread count of a JVM.|
|jvm_threads_peak|gauge|||Peak thread count of a JVM.|
|jvm_threads_started_total|counter|||Started thread count of a JVM.|
|jvm_threads_deadlocked|gauge|||Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers.|
|jvm_threads_deadlocked_monitor|gauge|||Cycles of JVM-threads that are in deadlock waiting to acquire object monitors.|
|jvm_threads_state|gauge|state||Current count of threads by state.|
|jvm_classes_loaded|gauge|||The number of classes that are currently loaded in the JVM.|
|jvm_classes_loaded_total|counter|||The total number of classes that have been loaded since the JVM has started execution.|
|jvm_classes_unloaded_total|counter|||The total number of classes that have been unloaded since the JVM has started execution.|
|jvm_info|gauge|version, vendor, runtime||JVM version info.|



## What Datomic transactor metrics are covered?

The following CloudWatch metrics that can be created by the transactor are supported:

- [x] Alarm
- [x] AlarmIndexingJobFailed
- [x] AlarmBackPressure
- [x] AlarmUnhandledException
- [x] Alarm{AnythingElse}
- [x] AvailableMB
- [ ] ClusterCreateFS
- [x] CreateEntireIndexMsec
- [x] CreateFulltextIndexMsec
- [x] Datoms
- [x] DBAddFulltextMsec
- [ ] FulltextSegments
- [x] GarbageSegments
- [x] HeartbeatMsec
- [x] HeartbeatMsec (samples)
- [ ] HeartMonitorMsec
- [x] IndexDatoms
- [x] IndexSegments
- [x] IndexWrites
- [x] IndexWriteMsec
- [ ] LogIngestBytes
- [ ] LogIngestMsec
- [x] LogWriteMsec
- [ ] Memcache
- [x] MemoryIndexMB
- [x] MetricReport
- [ ] ObjectCache
- [ ] MemcachedPutMsec
- [ ] MemcachedPutFailedMsec
- [x] RemotePeers
- [x] StorageBackoff (total time per transactor metric report)
- [x] StorageBackoff (total number of retries)
- [x] Storage{Get,Put}Bytes (throughput per transactor metric report)
- [x] Storage{Get,Put}Bytes (operations count per transactor metric report)
- [x] Storage{Get,Put}Msec
- [x] TransactionBatch
- [x] TransactionBytes (total volume of transaction data to log, peers)
- [x] TransactionDatoms (total datoms transacted)
- [x] TransactionDatoms (total transactions)
- [x] TransactionMsec (total time spent on transactions)
- [ ] Valcache
- [ ] ValcachePutMsec
- [ ] ValcachePutFailedMsec

The following additional metrics are calculated based on the metrics stated above:

- [x] Object Cache Hit Ratio

|Metric|Type|Labels|Unit|Description|
|------|----|------|----|-----------|
|datomic_alarms|gauge|kind||Number of alarms/problems that have occurred.|
|datomic_available_ram_bytes|gauge||bytes|Unused RAM on transactor in bytes.|
|datomic_object_cache_size|gauge|||Number of segments in the Datomic object cache.|
|datomic_object_cache_requests|gauge|||Number of requests to the Datomic object cache.|
|datomic_object_cache_hits_ratio|gauge|||Datomic object cache hit ratio.|
|datomic_remote_peers|gauge|||Number of remote peers connected.|
|datomic_successful_metric_reports|gauge|||Number of successful metric reports over a 1 min period.|
|datomic_transacted_datoms_total|counter|||Total number of transacted datoms.|
|datomic_transactions_total|counter|||Total number of transactions.|
|datomic_transactions_batch|gauge|||Number of transactions batched into a single write to the log.|
|datomic_transacted_bytes_total|counter||bytes|Total volume of transaction data to log, peers in bytes.|
|datomic_transactions_seconds_total|counter||seconds|Total time of transactions in seconds.|
|datomic_transactions_add_fulltext_seconds_total|counter||seconds|Total time of transactions spent to add fulltext in seconds.|
|datomic_transactions_write_log_seconds_total|counter||seconds|Total time of transactions spent writing to log per transaction batch in seconds.|
|datomic_datoms|counter|||Number of unique datoms in the index.|
|datomic_index_datoms|gauge|||Number of datoms stored by the index, all sorts.|
|datomic_index_segments|gauge|||Number of segments in the index.|
|datomic_index_writes|gauge|||Number of segments written by indexing job, reported at end.|
|datomic_index_writes_seconds|gauge||seconds|Time per index segment write in seconds.|
|datomic_index_creation_seconds|gauge||seconds|Time to create index in seconds, reported at end of indexing job.|
|datomic_index_fulltext_creation_seconds|gauge||seconds|Time to create fulltext portion of index in seconds.|
|datomic_memory_index_consumed_bytes|gauge||bytes|RAM consumed by memory index in bytes.|
|datomic_memory_index_fill_seconds|gauge||seconds|Estimate of the time to fill the memory index in seconds, given the current write load.|
|datomic_storage_write_operations_total|counter|||Total number of storage write operations.|
|datomic_storage_write_bytes_total|counter||bytes|Total number of bytes written to storage.|
|datomic_storage_write_seconds|gauge||seconds|Time spent writing to storage in seconds.|
|datomic_storage_read_operations_total|counter|||Total number of storage read operations.|
|datomic_storage_read_bytes_total|counter||bytes|Total number of bytes read from storage.|
|datomic_storage_read_seconds|gauge||seconds|Time spent reading from storage in seconds.|
|datomic_storage_backoff_seconds|gauge||seconds|Time spent in backoff/retry around calls to storage in seconds.|
|datomic_storage_backoff_retries_total|counter|||Total number of retried storage operations.|
|datomic_garbage_segments|gauge|||Number of garbage segments created.|
|datomic_heartbeats_seconds|gauge||seconds|Time spent writing to storage in seconds as part of the heartbeat (transactor writes location).|
|datomic_heartbeats|gauge|||Number of heartbeats.|



## Example Metrics

```
# HELP datomic_storage_read_seconds Time spent reading from storage in seconds.
# TYPE datomic_storage_read_seconds gauge
datomic_storage_read_seconds 0.452
# HELP datomic_heartbeats_seconds Time spent writing to storage in seconds as part of the heartbeat (transactor writes location).
# TYPE datomic_heartbeats_seconds gauge
datomic_heartbeats_seconds 60.001
# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
# TYPE jvm_gc_collection_seconds summary
jvm_gc_collection_seconds_count{gc="PS Scavenge",} 45.0
jvm_gc_collection_seconds_sum{gc="PS Scavenge",} 1.42
jvm_gc_collection_seconds_count{gc="PS MarkSweep",} 4.0
jvm_gc_collection_seconds_sum{gc="PS MarkSweep",} 0.429
# HELP datomic_transactions_batch Number of transactions batched into a single write to the log.
# TYPE datomic_transactions_batch gauge
datomic_transactions_batch 277.0
# HELP datomic_garbage_segments Number of garbage segments created.
# TYPE datomic_garbage_segments gauge
datomic_garbage_segments 784.0
# HELP jvm_classes_loaded The number of classes that are currently loaded in the JVM
# TYPE jvm_classes_loaded gauge
jvm_classes_loaded 19867.0
# HELP jvm_classes_loaded_total The total number of classes that have been loaded since the JVM has started execution
# TYPE jvm_classes_loaded_total counter
jvm_classes_loaded_total 19867.0
# HELP jvm_classes_unloaded_total The total number of classes that have been unloaded since the JVM has started execution
# TYPE jvm_classes_unloaded_total counter
jvm_classes_unloaded_total 0.0
# HELP datomic_object_cache_size Number of segments in the Datomic object cache.
# TYPE datomic_object_cache_size gauge
datomic_object_cache_size 1352.0
# HELP datomic_transactions_add_fulltext_seconds_total Total time of transactions spent to add fulltext in seconds.
# TYPE datomic_transactions_add_fulltext_seconds_total counter
datomic_transactions_add_fulltext_seconds_total 0.213
# HELP datomic_transacted_datoms_total Number of transacted datoms.
# TYPE datomic_transacted_datoms_total counter
datomic_transacted_datoms_total 1145674.0
# HELP datomic_storage_read_operations_total Total number of storage read operations.
# TYPE datomic_storage_read_operations_total counter
datomic_storage_read_operations_total 529.0
# HELP jvm_info JVM version info
# TYPE jvm_info gauge
jvm_info{version="1.8.0_222-b10",vendor="Oracle Corporation",runtime="OpenJDK Runtime Environment",} 1.0
# HELP datomic_storage_backoff_retries_total Total number of retried storage operations.
# TYPE datomic_storage_backoff_retries_total counter
datomic_storage_backoff_retries_total 0.0
# HELP datomic_index_segments Number of segments in the index.
# TYPE datomic_index_segments gauge
datomic_index_segments 1399.0
# HELP datomic_transactions_total Total number of transactions.
# TYPE datomic_transactions_total counter
datomic_transactions_total 444.0
# HELP datomic_transactions_write_log_seconds_total Total time of transactions spent writing to log per transaction batch in seconds.
# TYPE datomic_transactions_write_log_seconds_total counter
datomic_transactions_write_log_seconds_total 7.488999999999999
# HELP datomic_index_writes Number of segments written by indexing job, reported at end.
# TYPE datomic_index_writes gauge
datomic_index_writes 636.0
# HELP datomic_transacted_bytes_total Total volume of transaction data to log, peers in bytes.
# TYPE datomic_transacted_bytes_total counter
datomic_transacted_bytes_total 2.0042116E7
# HELP datomic_storage_write_bytes_total Total number of bytes written to storage.
# TYPE datomic_storage_write_bytes_total counter
datomic_storage_write_bytes_total 2.0352381E7
# HELP datomic_storage_write_operations_total Total number of storage write operations.
# TYPE datomic_storage_write_operations_total counter
datomic_storage_write_operations_total 1593.0
# HELP datomic_datoms Number of unique datoms in the index.
# TYPE datomic_datoms gauge
datomic_datoms 2437759.0
# HELP datomic_available_ram_bytes Unused RAM on transactor in bytes.
# TYPE datomic_available_ram_bytes gauge
datomic_available_ram_bytes 3.32E9
# HELP datomic_successful_metric_reports Number of successful metric reports over a 1 min period.
# TYPE datomic_successful_metric_reports gauge
datomic_successful_metric_reports 1.0
# HELP datomic_index_fulltext_creation_seconds Time to create fulltext portion of index in seconds.
# TYPE datomic_index_fulltext_creation_seconds gauge
datomic_index_fulltext_creation_seconds 0.0
# HELP datomic_remote_peers Number of remote peers connected.
# TYPE datomic_remote_peers gauge
datomic_remote_peers 6.0
# HELP jvm_memory_bytes_used Used bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_used gauge
jvm_memory_bytes_used{area="heap",} 1.05045048E9
jvm_memory_bytes_used{area="nonheap",} 1.56363984E8
# HELP jvm_memory_bytes_committed Committed (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_committed gauge
jvm_memory_bytes_committed{area="heap",} 4.178051072E9
jvm_memory_bytes_committed{area="nonheap",} 1.78470912E8
# HELP jvm_memory_bytes_max Max (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_max gauge
jvm_memory_bytes_max{area="heap",} 4.178051072E9
jvm_memory_bytes_max{area="nonheap",} -1.0
# HELP jvm_memory_bytes_init Initial bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_init gauge
jvm_memory_bytes_init{area="heap",} 4.294967296E9
jvm_memory_bytes_init{area="nonheap",} 2555904.0
# HELP jvm_memory_pool_bytes_used Used bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_used gauge
jvm_memory_pool_bytes_used{pool="Code Cache",} 3.4965248E7
jvm_memory_pool_bytes_used{pool="Metaspace",} 1.00482128E8
jvm_memory_pool_bytes_used{pool="Compressed Class Space",} 2.0916608E7
jvm_memory_pool_bytes_used{pool="PS Eden Space",} 6.47085208E8
jvm_memory_pool_bytes_used{pool="PS Survivor Space",} 9.269804E7
jvm_memory_pool_bytes_used{pool="PS Old Gen",} 3.10671416E8
# HELP jvm_memory_pool_bytes_committed Committed bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_committed gauge
jvm_memory_pool_bytes_committed{pool="Code Cache",} 3.5258368E7
jvm_memory_pool_bytes_committed{pool="Metaspace",} 1.1669504E8
jvm_memory_pool_bytes_committed{pool="Compressed Class Space",} 2.6517504E7
jvm_memory_pool_bytes_committed{pool="PS Eden Space",} 1.199570944E9
jvm_memory_pool_bytes_committed{pool="PS Survivor Space",} 1.14819072E8
jvm_memory_pool_bytes_committed{pool="PS Old Gen",} 2.863661056E9
# HELP jvm_memory_pool_bytes_max Max bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_max gauge
jvm_memory_pool_bytes_max{pool="Code Cache",} 2.5165824E8
jvm_memory_pool_bytes_max{pool="Metaspace",} -1.0
jvm_memory_pool_bytes_max{pool="Compressed Class Space",} 1.073741824E9
jvm_memory_pool_bytes_max{pool="PS Eden Space",} 1.202716672E9
jvm_memory_pool_bytes_max{pool="PS Survivor Space",} 1.14819072E8
jvm_memory_pool_bytes_max{pool="PS Old Gen",} 2.863661056E9
# HELP jvm_memory_pool_bytes_init Initial bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_init gauge
jvm_memory_pool_bytes_init{pool="Code Cache",} 2555904.0
jvm_memory_pool_bytes_init{pool="Metaspace",} 0.0
jvm_memory_pool_bytes_init{pool="Compressed Class Space",} 0.0
jvm_memory_pool_bytes_init{pool="PS Eden Space",} 1.073741824E9
jvm_memory_pool_bytes_init{pool="PS Survivor Space",} 1.78782208E8
jvm_memory_pool_bytes_init{pool="PS Old Gen",} 2.863661056E9
# HELP jvm_threads_current Current thread count of a JVM
# TYPE jvm_threads_current gauge
jvm_threads_current 90.0
# HELP jvm_threads_daemon Daemon thread count of a JVM
# TYPE jvm_threads_daemon gauge
jvm_threads_daemon 45.0
# HELP jvm_threads_peak Peak thread count of a JVM
# TYPE jvm_threads_peak gauge
jvm_threads_peak 94.0
# HELP jvm_threads_started_total Started thread count of a JVM
# TYPE jvm_threads_started_total counter
jvm_threads_started_total 121.0
# HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
# TYPE jvm_threads_deadlocked gauge
jvm_threads_deadlocked 0.0
# HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors
# TYPE jvm_threads_deadlocked_monitor gauge
jvm_threads_deadlocked_monitor 0.0
# HELP datomic_transactions_seconds_total Total time of transactions in seconds.
# TYPE datomic_transactions_seconds_total counter
datomic_transactions_seconds_total 339.009
# HELP datomic_memory_index_consumed_bytes RAM consumed by memory index in bytes.
# TYPE datomic_memory_index_consumed_bytes gauge
datomic_memory_index_consumed_bytes 3.5E7
# HELP datomic_alarms Number of alarms/problems that have occurred distinguished by their kind.
# TYPE datomic_alarms gauge
datomic_alarms{kind="index-job-failed",} 0.0
datomic_alarms{kind="other",} 0.0
# HELP datomic_heartbeats Number of heartbeats.
# TYPE datomic_heartbeats gauge
datomic_heartbeats 12.0
# HELP datomic_object_cache_requests Number of requests to the Datomic object cache.
# TYPE datomic_object_cache_requests gauge
datomic_object_cache_requests 821.0
# HELP datomic_storage_read_bytes_total Total number of bytes read from storage.
# TYPE datomic_storage_read_bytes_total counter
datomic_storage_read_bytes_total 495987.0
# HELP datomic_index_creation_seconds Time to create index in seconds, reported at end of indexing job.
# TYPE datomic_index_creation_seconds gauge
datomic_index_creation_seconds 11.94
# HELP datomic_index_datoms Number of datoms stored by the index, all sorts.
# TYPE datomic_index_datoms gauge
datomic_index_datoms 6602659.0
# HELP datomic_object_cache_hits_ratio Datomic object cache hit ratio.
# TYPE datomic_object_cache_hits_ratio gauge
datomic_object_cache_hits_ratio 0.7403065825067628
# HELP datomic_storage_write_seconds Time spent writing to storage in seconds.
# TYPE datomic_storage_write_seconds gauge
datomic_storage_write_seconds 2.508
# HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
# TYPE process_cpu_seconds_total counter
process_cpu_seconds_total 244.54
# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.571923073319E9
# HELP process_open_fds Number of open file descriptors.
# TYPE process_open_fds gauge
process_open_fds 454.0
# HELP process_max_fds Maximum number of open file descriptors.
# TYPE process_max_fds gauge
process_max_fds 1048576.0
# HELP process_virtual_memory_bytes Virtual memory size in bytes.
# TYPE process_virtual_memory_bytes gauge
process_virtual_memory_bytes 1.0496651264E10
# HELP process_resident_memory_bytes Resident memory size in bytes.
# TYPE process_resident_memory_bytes gauge
process_resident_memory_bytes 2.107416576E9
# HELP datomic_storage_backoff_seconds Time spent in backoff/retry around calls to storage in seconds.
# TYPE datomic_storage_backoff_seconds gauge
datomic_storage_backoff_seconds 0.0
# HELP datomic_index_writes_seconds Time per index segment write in seconds.
# TYPE datomic_index_writes_seconds gauge
datomic_index_writes_seconds 3.97
```

## Troubleshooting

__Problem:__ The transactor refuses to start because there is an error related to netty (some methods cannot be found).

__Solution:__ Depending on the Datomic version in use the netty version that comes with it may be too old missing methods required by this project. Resolve this issue by replacing the netty-all*.jar in Datomic's `/lib` directory with a newer one. _This is also covered by the docker example in the __examples__ section of the repository._   

## License

Copyright © 2019 LIFE Research Center

Distributed under the Eclipse Public License.
