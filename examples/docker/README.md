# Docker Example

A simple example of a containerized Datomic transactor with a registered metrics collector backed by a cassandra database.

## Prerequisites

* you have to have a Datomic account associated with a valid Pro license
* your license key has to be pasted into the `transactor.properties` file under the `license-key` configuration entry before build
* cassandra database is required

## Build the container image

Run the following command in order to build the container image:

```
docker build -t --build-arg DATOMIC_ACC_USER=<username> --build-arg DATOMIC_ACC_PASS=<password> datomic-tx-metrics:<version> .
``` 

## Run the container

The example assumes that the transactor can use an existing cassandra keyspace and table. **At this point the keyspace has to be `datomic`** _(can be changed at will by adjusting the `start.sh` and/or `transactor.properties` file)._

In order to successfully start the transactor the cassandra database to be used has to be configured beforehand. Tools needed to do so can be obtained from the official [Cassandra Download Page](http://cassandra.apache.org/download/) (they're part of the archive in the `bin` directory).

If not already done you can do so by connecting to the database using `cqlsh`:
```
cqlsh <cassandra-host>
```

Next create the `datomic` keyspace:
```
CREATE KEYSPACE IF NOT EXISTS datomic WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor': <rep-factor> };
```

Subsequently create the `datomic` table within the previously created keyspace:
```
CREATE TABLE IF NOT EXISTS datomic.datomic
(
  id text PRIMARY KEY,
  rev bigint,
  map text,
  val blob
);
```

Finally run the following command to start a container using the built container image:
```
docker run --name "datomic-transactor" --rm -d \
    -e CASSANDRA_HOST=<cass-host> \
    -e CASSANDRA_TABLE=<cass-table> \
    -e METRICS_PORT=<metrics-server-port> \
    -p <metrics-server-port>:<metrics-server-port> \
    datomic-tx-metrics:<version>
```

**Note:** _If you are using the default port (11509) of the metrics collector's web server and not using the `METRICS_PORT` environment variable make sure to still expose that port since it's not exposed by default. Otherwise you won't be able to scrape it._ 
