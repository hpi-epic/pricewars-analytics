#paths to kafka and flink. Please adapt according to your configuration.
FLINK_PATH=/opt/flink
KAFKA_PATH=/opt/kafka

#start kafka and flink. If both are already running, comment out this section
#Please note that for this part admin rights are necessary to start them.
nohup "$KAFKA_PATH"/bin/kafka-server-start.sh "$KAFKA_PATH"/config/server.properties > "$KAFKA_PATH"/kafka.log 2>&1 &
"$FLINK_PATH"/bin/start-local.sh

#compile jars and start jobs
sbt assembly
for file in ./target/jars/*; do
    if [ "${file}" != "${file%.jar}" ];then
        echo "Adding $file as flink job..."
        "$FLINK_PATH"/bin/flink run -d $file
    fi
done
