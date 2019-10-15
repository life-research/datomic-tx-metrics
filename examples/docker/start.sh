#!/usr/bin/env bash

sed -i "/host=0.0.0.0/a alt-host=${ALT_HOST:-127.0.0.1}" transactor.properties
sed -i "s/port=4334/port=${PORT:-4334}/" transactor.properties

## Cassandra
sed -i "s/cassandra-host=/cassandra-host=${CASSANDRA_HOST}/" transactor.properties
sed -i "s/cassandra-table=datomic.datomic/cassandra-table=datomic.${CASSANDRA_TABLE:-datomic}/" transactor.properties

## Memory Settings
sed -i "s/memory-index-threshold=32m/memory-index-threshold=${MEMORY_INDEX_THRESHOLD:-32m}/" transactor.properties
sed -i "s/memory-index-max=512m/memory-index-max=${MEMORY_INDEX_MAX:-512m}/" transactor.properties
sed -i "s/object-cache-max=1g/object-cache-max=${OBJECT_CACHE_MAX:-1g}/" transactor.properties
sed -i "s/write-concurrency=4/write-concurrency=${WRITE_CONCURRENCY:-4}/" transactor.properties
sed -i "s/read-concurrency=8/read-concurrency=${READ_CONCURRENCY:-8}/" transactor.properties

## Logging
sed -i "s/root level=\"INFO\"/root level=\"${LOG_LEVEL_ROOT:-INFO}\"/" bin/logback.xml

# Start transactor
exec bin/transactor "-Xms${XMX:-4g}" "-Xmx${XMX:-4g}" transactor.properties
