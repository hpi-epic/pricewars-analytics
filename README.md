# Analytics

This repository provides [Apache Flink](https://flink.apache.org/) services for the analysis of the data generated in the Pricewars-simulation.

The input source for these services is the [Apache KAFKA](https://kafka.apache.org/) log, which the simulation uses to
 store its log entries.
 
 At the moment, we generate the following analytics per merchant:
 * Profit per minute, per hour and in total
 * Revenue per minute, per hour and in total
 * Marketshare in total in relation to the profit, the revenue and the number of items sold 
 
The meta repository containing general information can be found [here](https://github.com/hpi-epic/masterproject-pricewars).

## Application Overview

| Repo | Branch 	| Deployment to  	| Status | Description |
|--- |---	|---	|---  |---   |
| [UI](https://github.com/hpi-epic/pricewars-mgmt-ui) | master  	|  [vm-mpws2016hp1-02.eaalab.hpi.uni-potsdam.de](http://vm-mpws2016hp1-02.eaalab.hpi.uni-potsdam.de) 	| [ ![Codeship Status for hpi-epic/pricewars-mgmt-ui](https://app.codeship.com/projects/d91a8460-88c2-0134-a385-7213830b2f8c/status?branch=master)](https://app.codeship.com/projects/184009) | Stable |
| [Consumer](https://github.com/hpi-epic/pricewars-consumer) | master  	|  [vm-mpws2016hp1-01.eaalab.hpi.uni-potsdam.de](http://vm-mpws2016hp1-01.eaalab.hpi.uni-potsdam.de) | [ ![Codeship Status for hpi-epic/pricewars-consumer](https://app.codeship.com/projects/96f32950-7824-0134-c83e-5251019101b9/status?branch=master)](https://app.codeship.com/projects/180119) | Stable |
| [Producer](https://github.com/hpi-epic/pricewars-producer) | master  	|  [vm-mpws2016hp1-03eaalab.hpi.uni-potsdam.de](http://vm-mpws2016hp1-03.eaalab.hpi.uni-potsdam.de) | [ ![Codeship Status for hpi-epic/pricewars-producer](https://app.codeship.com/projects/0328e450-88c6-0134-e3d6-7213830b2f8c/status?branch=master)](https://app.codeship.com/projects/184016) | Stable |
| [Marketplace](https://github.com/hpi-epic/pricewars-marketplace) | master  	|  [vm-mpws2016hp1-04.eaalab.hpi.uni-potsdam.de/marketplace](http://vm-mpws2016hp1-04.eaalab.hpi.uni-potsdam.de/marketplace/offers) 	| [ ![Codeship Status for hpi-epic/pricewars-marketplace](https://app.codeship.com/projects/e9d9b3e0-88c5-0134-6167-4a60797e4d29/status?branch=master)](https://app.codeship.com/projects/184015) | Stable |
| [Merchant](https://github.com/hpi-epic/pricewars-merchant) | master  	|  [vm-mpws2016hp1-06.eaalab.hpi.uni-potsdam.de/](http://vm-mpws2016hp1-06.eaalab.hpi.uni-potsdam.de/) 	| [ ![Codeship Status for hpi-epic/pricewars-merchant](https://app.codeship.com/projects/a7d3be30-88c5-0134-ea9c-5ad89f4798f3/status?branch=master)](https://app.codeship.com/projects/184013) | Stable |
| [Kafka RESTful API](https://github.com/hpi-epic/pricewars-kafka-rest) | master  	|  [vm-mpws2016hp1-05.eaalab.hpi.uni-potsdam.de](http://vm-mpws2016hp1-05.eaalab.hpi.uni-potsdam.de) 	|  [ ![Codeship Status for hpi-epic/pricewars-kafka-rest](https://app.codeship.com/projects/f59aa150-92f0-0134-8718-4a1d78af514c/status?branch=master)](https://app.codeship.com/projects/186252) | Stable |

## Requirements

The flink services are written in Scala and managed with sbt. Ensure to have a sbt installed and set up on your computer (see [the reference](http://www.scala-sbt.org/0.13/docs/Setup.html) for more information on getting started). In order to build the executable jar files, you need Java (version 6). Sbt will fetch the required Scala version and necessary dependencies automatically.

To run the flink services, you need a running Flink setup.

## Setup

Open a console in the cloned repository and enter `sbt assembly`.
This command will build the jar files and put them all into the folder `target/jars/`.

There are now two ways to start the processing of these Flink services.
Both require an existing installation of Flink version 1.1.3 or higher in order to work.
In Flink, each services consumes a so-called _slot_. 
Increasing the parallel execution level for Flink services increases the number of consumed slots per service, too.
Each task manager of a Flink setup provides one or more slots for processing tasks.
In order to ensure that all our services can run at least single-threaded, please ensure that you have at least 3 slots
in your Flink setup (see [Flink slot configuration](https://ci.apache.org/projects/flink/flink-docs-release-1.2/setup/config.html#configuring-taskmanager-processing-slots)).

The first way is to use the web interface (usually exposed via port 8081) of Flink.
Click on `Submit new Job` on the left menu and upload your jar files.
Then for each service, select your jar file and click on submit.

The second way is to use the command line.
Start a new job just with `path/to/flink/bin/flink run path/to/repository/target/jars/NameOfService.jar`.

In case you use a standalone machine and not the docker setup, please make sure to install the components in `/opt/` or `ln -s` them like `cd /opt && ln -s $path/to/kafka$ kafka`

## Configuration

The flink services need the url of the KAFKA log, which can be found (and - if necessary - updated) in the `application.conf` files in the `resources/` folder of each service's project path.
When wanted, the conf file allows to prefer specific environment variables by overriding the same config key with `${?ENV_VAR_NAME}`.

## Concept
 
 To generate the profit, revenue and marketshare per merchant, we currently use three flink services. These three services consume log entries from two different topics: `buyOffer` (containing all sales on the marketplace, including failed ones) and `producer` (containing all products distributed to the merchants).
 
To store the results of these analysis, Flink logs the values back to the same KAFKA instance into different topics.

#### Service 1: Sliding Window Profit and Revenue Aggregation
The source code and sbt project for this service is stored in the folder `aggregatedProfit/`.

This service calculates the aggregated profit and revenue for each merchant.

The profit is defined as the number of items sold (amount) times the price they are sold for to the consumers (selling price)
 minus the number of items sold times the price they are bought for from the producer (purchase price): 
`amount * (selling price - purchase price)`. 
The revenue is defined as the number of items sold (amount) times the price they are sold for to the consumers (selling price):
`amount * selling price`.

The calculations are based on _sliding windows_, we provide for both aggregations two streams with different time intervals.
One stream performs the aggregation of the values per minute and publishes updates every 10 seconds.
The second stream aggregates the values per hour and publishes updates every minute.

The files `ProfitStream.scala` and `RevenueStream.scala` contain the algorithms, whereas `SlidingWindowAggregations.scala`
 ties the input streams together with these algorithms and thus is the actual Flink service.

#### Service 2: Cumulative Profit and Revenue Aggregation
The folder `cumulativeProfit/` contains the source code and sbt project for this service.

It calculates the aggregated profit and revenue for each merchant, too, but instead of sliding windows this services uses _global windows_.

Global windows are used for cumulative aggregation, which means the total value since the beginning of the simulation to
 the current point in time.
Both streams publish the new values for each merchant once every minute.
 
The structure is the same as for the first service.
 
#### Service 3: Cumulative Market Share
In the folder `cumulativeMarketShare/`, the source code and sbt project for the market share service is stored.

For the calculation of the market share of a merchant, three methods are used and exposed as streams.
The most simple method is based on the amount of sold items.
The second uses the revenue and the third is based on the profit.

For cumulative aggregation, global windows are used like in the second service.
Every minute, the new market share situation is published by the three streams.

## Issues

Currently there is a known issue when you want to start a new simulation after you have already run a simulation in the same setup.
Our services are not able to detect the end of a run and the start of a new one, because there is no concept of "a run" existent.
Therefore, the **cumulative services are not reset**.

Although this does not influence the cumulative profit and revenue aggregation, it has a significant impact on the market share calculation.
The old values are still regarded in the calculation, hence the market shares do not reflect the actual state.

In order to get a clean state after stopping a simulation, stop all running Flink jobs as well as the KAFKA reverse proxy,
 delete all entries from the kafka log and restart the Flink services and the KAFKA reverse proxy.
In the docker setup however, after stopping the simulation with docker-compose stop, it is sufficient just to delete
the docker-mounts folder as it contains the whole state.

In case that a service terminates unexpectedly, a log is provided by Flink that also contains the error messages and 
 stack traces of failed jobs.
If the failure is caused by a malformed log message, your only choice is to empty the KAFKA log, because a Flink service 
 resumes its execution at its last checkpoint after restart.
Therefore, it would try to process the same malformed message again and fail again.
In any case, do **never log manually into topics** consumed by our Flink services and use json libraries and stick to the 
 current data schema when working on other pricewars related services that log into KAFKA.
