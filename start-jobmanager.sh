#!/bin/bash

/opt/flink/bin/docker-entrypoint.sh jobmanager &

/analytics/wait-for-it.sh flink-jobmanager:6123 -t 0

for file in /analytics/*; do
    if [ "${file}" != "${file%.jar}" ];then
        echo "Adding $file as flink job..."
        sleep 5
        /opt/flink/bin/flink run -d $file
    fi
done
