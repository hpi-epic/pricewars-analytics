#!/bin/bash

for file in /analytics/*; do
    if [ "${file}" != "${file%.jar}" ];then
        echo "Adding $file as flink job..."
        /opt/flink/bin/flink run -d $file
    fi
done

/opt/flink/bin/docker-entrypoint.sh jobmanager
