#!/bin/bash

/analytics/wait-for-it.sh flink-jobmanager:6123 -t 0

echo -e 'taskmanager.rpc.port: 6122\ntaskmanager.data.port: 6121' >> /opt/flink/conf/flink-conf.yaml

/opt/flink/bin/docker-entrypoint.sh taskmanager
