#!/bin/bash

/analytics/wait-for-it.sh flink-jobmanager:6123 -t 0

# Edit config file to suppress INFO messages on console
find /opt/flink/conf -type f -exec sed -i "s/INFO, console/WARN, console/g" {} \;

/opt/flink/bin/docker-entrypoint.sh taskmanager &

# Getting the process and waiting for it to ensure the container will stay up
PID_TASKMANAGER=$!

echo -e 'taskmanager.rpc.port: 6122\ntaskmanager.data.port: 6121' >> /opt/flink/conf/flink-conf.yaml

/analytics/wait-for-it.sh flink-taskmanager:6121 -t 0
STDOUT_LOG_NUMBER=`find /opt/flink-1.1.3/log/ -name "*.out" | wc -l`
STDOUT_LOG_FILE_NAME="/opt/flink-1.1.3/log/flink--taskmanager-"$STDOUT_LOG_NUMBER"-"`hostname`".out"
echo "TaskManager initialized "`date`" with this empty log file to prevent crashs." >> $STDOUT_LOG_FILE_NAME

wait $PID_TASKMANAGER
