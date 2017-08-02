#!/bin/sh

#paths to kafka and flink. Please adapt according to your configuration.
FLINK_PATH=/opt/flink
KAFKA_PATH=/opt/kafka

"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic marketSituation
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeRevenueBasedMarketshare
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic updateOffer
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeRevenueBasedMarketshareHourly
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeRevenueBasedMarketshareDaily
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeTurnoverBasedMarketshare
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeTurnoverBasedMarketshareHourly
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeTurnoverBasedMarketshareDaily
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeAmountBasedMarketshare
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeAmountBasedMarketshareHourly
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic cumulativeAmountBasedMarketshareDaily
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic revenue
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic buyOffer
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic buyOffers
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic producer
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic revenuePerMinute
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic profitPerMinute
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic revenuePerHour
"$KAFKA_PATH"/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic profitPerHour

sudo service loggerapp restart

"$FLINK_PATH"/bin/flink list -r | awk '{split($0, a, " : "); print a[2]}' | while read line; do
    [ -z "$line" ] && continue
    "$FLINK_PATH"/bin/flink cancel $line
done

for file in ./target/jars/*; do
    if [ "${file}" != "${file%.jar}" ];then
        echo "Adding $file as flink job..."
        "$FLINK_PATH"/bin/flink run -d $file
    fi
done
