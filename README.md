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
|process_cpu_seconds_total|counter||sec|Total user and system CPU time spent in seconds.|
|process_start_time_seconds|gauge||sec|Start time of the process since unix epoch in seconds.|
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
|jvm_gc_collection_seconds_count|summary|gc|sec|Times a given JVM garbage collector ran.|
|jvm_gc_collection_seconds_sum|summary|gc|sec|Time spent in a given JVM garbage collector in seconds.|
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
|datomic_transactions_sec_total|counter||sec|Total time of transactions in sec.|
|datomic_transactions_add_fulltext_sec_total|counter||sec|Total time of transactions spent to add fulltext in seconds.|
|datomic_transactions_write_log_sec_total|counter||sec|Total time of transactions spent writing to log per transaction batch in seconds.|
|datomic_datoms|counter|||Number of unique datoms in the index.|
|datomic_index_datoms|gauge|||Number of datoms stored by the index, all sorts.|
|datomic_index_segments|gauge|||Number of segments in the index.|
|datomic_index_writes|gauge|||Number of segments written by indexing job, reported at end.|
|datomic_index_writes_sec|gauge||sec|Time per index segment write in seconds.|
|datomic_index_creation_sec|gauge||sec|Time to create index in seconds, reported at end of indexing job.|
|datomic_index_fulltext_creation_sec|gauge||sec|Time to create fulltext portion of index in seconds.|
|datomic_memory_index_consumed_bytes|gauge||bytes|RAM consumed by memory index in bytes.|
|datomic_memory_index_fill_sec|gauge||sec|Estimate of the time to fill the memory index in seconds, given the current write load.|
|datomic_storage_write_operations_total|counter|||Total number of storage write operations.|
|datomic_storage_write_bytes_total|counter||bytes|Total number of bytes written to storage.|
|datomic_storage_write_sec|gauge||sec|Time spent writing to storage in seconds.|
|datomic_storage_read_operations_total|counter|||Total number of storage read operations.|
|datomic_storage_read_bytes_total|counter||bytes|Total number of bytes read from storage.|
|datomic_storage_read_sec|gauge||sec|Time spent reading from storage in seconds.|
|datomic_storage_backoff_sec|gauge||sec|Time spent in backoff/retry around calls to storage in seconds.|
|datomic_storage_backoff_retries_total|counter|||Total number of retried storage operations.|
|datomic_garbage_segments|gauge|||Number of garbage segments created.|
|datomic_heartbeats_sec|gauge||sec|Time spent writing to storage in seconds as part of the heartbeat (transactor writes location).|
|datomic_heartbeats|gauge|||Number of heartbeats.|



## Example Metrics

```
# HELP datomic_storage_backoff_retries_total Total number of retried storage operations.
# TYPE datomic_storage_backoff_retries_total counter
datomic_storage_backoff_retries_total 0.0
# HELP jvm_memory_bytes_used Used bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_used gauge
jvm_memory_bytes_used{area="heap",} 1.576059584E9
jvm_memory_bytes_used{area="nonheap",} 1.54890304E8
# HELP jvm_memory_bytes_committed Committed (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_committed gauge
jvm_memory_bytes_committed{area="heap",} 4.294967296E9
jvm_memory_bytes_committed{area="nonheap",} 1.77627136E8
# HELP jvm_memory_bytes_max Max (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_max gauge
jvm_memory_bytes_max{area="heap",} 4.294967296E9
jvm_memory_bytes_max{area="nonheap",} -1.0
# HELP jvm_memory_bytes_init Initial bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_init gauge
jvm_memory_bytes_init{area="heap",} 4.294967296E9
jvm_memory_bytes_init{area="nonheap",} 2555904.0
# HELP jvm_memory_pool_bytes_used Used bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_used gauge
jvm_memory_pool_bytes_used{pool="Code Cache",} 3.4133632E7
jvm_memory_pool_bytes_used{pool="Metaspace",} 9.9906008E7
jvm_memory_pool_bytes_used{pool="Compressed Class Space",} 2.0850664E7
jvm_memory_pool_bytes_used{pool="G1 Eden Space",} 7.0254592E8
jvm_memory_pool_bytes_used{pool="G1 Survivor Space",} 1.65675008E8
jvm_memory_pool_bytes_used{pool="G1 Old Gen",} 7.07838656E8
# HELP jvm_memory_pool_bytes_committed Committed bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_committed gauge
jvm_memory_pool_bytes_committed{pool="Code Cache",} 3.5127296E7
jvm_memory_pool_bytes_committed{pool="Metaspace",} 1.16076544E8
jvm_memory_pool_bytes_committed{pool="Compressed Class Space",} 2.6423296E7
jvm_memory_pool_bytes_committed{pool="G1 Eden Space",} 1.74063616E9
jvm_memory_pool_bytes_committed{pool="G1 Survivor Space",} 1.65675008E8
jvm_memory_pool_bytes_committed{pool="G1 Old Gen",} 2.388656128E9
# HELP jvm_memory_pool_bytes_max Max bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_max gauge
jvm_memory_pool_bytes_max{pool="Code Cache",} 2.5165824E8
jvm_memory_pool_bytes_max{pool="Metaspace",} -1.0
jvm_memory_pool_bytes_max{pool="Compressed Class Space",} 1.073741824E9
jvm_memory_pool_bytes_max{pool="G1 Eden Space",} -1.0
jvm_memory_pool_bytes_max{pool="G1 Survivor Space",} -1.0
jvm_memory_pool_bytes_max{pool="G1 Old Gen",} 4.294967296E9
# HELP jvm_memory_pool_bytes_init Initial bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_init gauge
jvm_memory_pool_bytes_init{pool="Code Cache",} 2555904.0
jvm_memory_pool_bytes_init{pool="Metaspace",} 0.0
jvm_memory_pool_bytes_init{pool="Compressed Class Space",} 0.0
jvm_memory_pool_bytes_init{pool="G1 Eden Space",} 2.26492416E8
jvm_memory_pool_bytes_init{pool="G1 Survivor Space",} 0.0
jvm_memory_pool_bytes_init{pool="G1 Old Gen",} 4.06847488E9
# HELP datomic_garbage_segments Number of garbage segments created.
# TYPE datomic_garbage_segments gauge
datomic_garbage_segments 2334.0
# HELP datomic_index_writes_msec Time per index segment write.
# TYPE datomic_index_writes_msec gauge
datomic_index_writes_msec 1707.0
# HELP datomic_index_creation_msec Time to create index in msec, reported at end of indexing job.
# TYPE datomic_index_creation_msec gauge
datomic_index_creation_msec 32430.0
# HELP datomic_index_fulltext_creation_msec Time to create fulltext portion of index in msec.
# TYPE datomic_index_fulltext_creation_msec gauge
datomic_index_fulltext_creation_msec 0.0
# HELP datomic_successful_metric_reports Number of successful metric reports over a 1 min period.
# TYPE datomic_successful_metric_reports gauge
datomic_successful_metric_reports 1.0
# HELP datomic_heartbeats Number of heartbeats.
# TYPE datomic_heartbeats gauge
datomic_heartbeats 12.0
# HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
# TYPE process_cpu_seconds_total counter
process_cpu_seconds_total 276.92
# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.571915290017E9
# HELP process_open_fds Number of open file descriptors.
# TYPE process_open_fds gauge
process_open_fds 530.0
# HELP process_max_fds Maximum number of open file descriptors.
# TYPE process_max_fds gauge
process_max_fds 1048576.0
# HELP process_virtual_memory_bytes Virtual memory size in bytes.
# TYPE process_virtual_memory_bytes gauge
process_virtual_memory_bytes 1.0579365888E10
# HELP process_resident_memory_bytes Resident memory size in bytes.
# TYPE process_resident_memory_bytes gauge
process_resident_memory_bytes 3.939934208E9
# HELP datomic_index_writes Number of segments written by indexing job, reported at end.
# TYPE datomic_index_writes gauge
datomic_index_writes 2103.0
# HELP datomic_datoms Number of unique datoms in the index.
# TYPE datomic_datoms gauge
datomic_datoms 1.6888212E7
# HELP datomic_transactions_write_log_msec_total Total time of transactions spent writing to log per transaction batch.
# TYPE datomic_transactions_write_log_msec_total counter
datomic_transactions_write_log_msec_total 6292.0
# HELP datomic_transactions_add_fulltext_msec_total Total time of transactions spent to add fulltext.
# TYPE datomic_transactions_add_fulltext_msec_total counter
datomic_transactions_add_fulltext_msec_total 147.0
# HELP datomic_index_datoms Number of datoms stored by the index, all sorts.
# TYPE datomic_index_datoms gauge
datomic_index_datoms 4.5733616E7
# HELP datomic_transacted_bytes_total Total volume of transaction data to log, peers in bytes.
# TYPE datomic_transacted_bytes_total counter
datomic_transacted_bytes_total 3.013932E7
# HELP datomic_storage_write_operations_total Total number of storage write operations.
# TYPE datomic_storage_write_operations_total counter
datomic_storage_write_operations_total 4148.0
# HELP datomic_transactions_total Total number of transactions.
# TYPE datomic_transactions_total counter
datomic_transactions_total 657.0
# HELP datomic_storage_read_bytes_total Total number of bytes read from storage.
# TYPE datomic_storage_read_bytes_total counter
datomic_storage_read_bytes_total 1049555.0
# HELP datomic_storage_read_msec Time spent reading from storage.
# TYPE datomic_storage_read_msec gauge
datomic_storage_read_msec 51.0
# HELP datomic_transactions_msec_total Total time of transactions in msec.
# TYPE datomic_transactions_msec_total counter
datomic_transactions_msec_total 415808.0
# HELP datomic_object_cache_size Number of segments in the Datomic object cache.
# TYPE datomic_object_cache_size gauge
datomic_object_cache_size 3819.0
# HELP datomic_storage_write_bytes_total Total number of bytes written to storage.
# TYPE datomic_storage_write_bytes_total counter
datomic_storage_write_bytes_total 6.2769877E7
# HELP datomic_heartbeats_msec Time spent writing to storage as part of the heartbeat (transactor writes location).
# TYPE datomic_heartbeats_msec gauge
datomic_heartbeats_msec 60000.0
# HELP datomic_transacted_datoms_total Number of transacted datoms.
# TYPE datomic_transacted_datoms_total counter
datomic_transacted_datoms_total 1718506.0
# HELP datomic_alarms Number of alarms/problems that have occurred distinguished by their kind.
# TYPE datomic_alarms gauge
datomic_alarms{kind="index-job-failed",} 0.0
datomic_alarms{kind="other",} 0.0
# HELP datomic_available_ram_megabytes Unused RAM on transactor in MB.
# TYPE datomic_available_ram_megabytes gauge
datomic_available_ram_megabytes 1490.0
# HELP datomic_storage_write_msec Time spent writing to storage.
# TYPE datomic_storage_write_msec gauge
datomic_storage_write_msec 1099.0
# HELP datomic_object_cache_hits_ratio Datomic object cache hit ratio.
# TYPE datomic_object_cache_hits_ratio gauge
datomic_object_cache_hits_ratio 0.8896067415730337
# HELP datomic_index_segments Number of segments in the index.
# TYPE datomic_index_segments gauge
datomic_index_segments 9374.0
# HELP datomic_object_cache_requests Number of requests to the Datomic object cache.
# TYPE datomic_object_cache_requests gauge
datomic_object_cache_requests 3167.0
# HELP datomic_remote_peers Number of remote peers connected.
# TYPE datomic_remote_peers gauge
datomic_remote_peers 1.0
# HELP datomic_memory_index_consumed_megabytes RAM consumed by memory index in MB.
# TYPE datomic_memory_index_consumed_megabytes gauge
datomic_memory_index_consumed_megabytes 4.0
# HELP jvm_classes_loaded The number of classes that are currently loaded in the JVM
# TYPE jvm_classes_loaded gauge
jvm_classes_loaded 19783.0
# HELP jvm_classes_loaded_total The total number of classes that have been loaded since the JVM has started execution
# TYPE jvm_classes_loaded_total counter
jvm_classes_loaded_total 19783.0
# HELP jvm_classes_unloaded_total The total number of classes that have been unloaded since the JVM has started execution
# TYPE jvm_classes_unloaded_total counter
jvm_classes_unloaded_total 0.0
# HELP datomic_transactions_batch Number of transactions batched into a single write to the log.
# TYPE datomic_transactions_batch gauge
datomic_transactions_batch 358.0
# HELP datomic_storage_read_operations_total Total number of storage read operations.
# TYPE datomic_storage_read_operations_total counter
datomic_storage_read_operations_total 757.0
# HELP jvm_info JVM version info
# TYPE jvm_info gauge
jvm_info{version="1.8.0_222-b10",vendor="Oracle Corporation",runtime="OpenJDK Runtime Environment",} 1.0
# HELP jvm_threads_current Current thread count of a JVM
# TYPE jvm_threads_current gauge
jvm_threads_current 69.0
# HELP jvm_threads_daemon Daemon thread count of a JVM
# TYPE jvm_threads_daemon gauge
jvm_threads_daemon 33.0
# HELP jvm_threads_peak Peak thread count of a JVM
# TYPE jvm_threads_peak gauge
jvm_threads_peak 75.0
# HELP jvm_threads_started_total Started thread count of a JVM
# TYPE jvm_threads_started_total counter
jvm_threads_started_total 91.0
# HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
# TYPE jvm_threads_deadlocked gauge
jvm_threads_deadlocked 0.0
# HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors
# TYPE jvm_threads_deadlocked_monitor gauge
jvm_threads_deadlocked_monitor 0.0
# HELP datomic_storage_backoff_msec Time spent in backoff/retry around calls to storage.
# TYPE datomic_storage_backoff_msec gauge
datomic_storage_backoff_msec 0.0
# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
# TYPE jvm_gc_collection_seconds summary
jvm_gc_collection_seconds_count{gc="G1 Young Generation",} 49.0
jvm_gc_collection_seconds_sum{gc="G1 Young Generation",} 1.812
jvm_gc_collection_seconds_count{gc="G1 Old Generation",} 0.0
jvm_gc_collection_seconds_sum{gc="G1 Old Generation",} 0.0
```

## Troubleshooting

__Problem:__ The transactor refuses to start because there is an error related to netty (some methods cannot be found).

__Solution:__ Depending on the Datomic version in use the netty version that comes with it may be too old missing methods required by this project. Resolve this issue by replacing the netty-all*.jar in Datomic's `/lib` directory with a newer one. _This is also covered by the docker example in the __examples__ section of the repository._   

## License

Copyright © 2019 LIFE Research Center

Distributed under the Eclipse Public License.
