#!/bin/bash

# Edit config file to suppress INFO messages on console
find /opt/flink/conf -type f -exec sed -i "s/INFO, console/WARN, console/g" {} \;

/docker-entrypoint.sh jobmanager &

# Getting the process and waiting for it to ensure the container will stay up
PID_JOBMANAGER=$!

/analytics/wait-for-it.sh flink-jobmanager:6123 -t 0

for file in /analytics/target/jars/*; do
    echo "Adding $file as flink job..."
    /opt/flink/bin/flink run -d $file
done

wait $PID_JOBMANAGER
