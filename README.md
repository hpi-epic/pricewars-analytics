# pricewars-analytics

This repository provides [Apache Flink](https://flink.apache.org/) services, which aggregate raw log values of the pricewars simulation from [Apache KAFKA](https://kafka.apache.org/) and writes the results back to KAFKA. Currently 3 services are provided:

1. `aggregatedProfit/`: A minutly and hourly aggregated profit and revenue per merchant based on moving windows (updated every 10s/1m).
2. `cumulativeProfit/`: A cumulative (the total from the beginning of the simulation) profit and revenue per merchant, minutly updated.
3. `cumulativeMarketShare/`: A cumulative (the total from the beginning of the simulation) marketshare per merchant, based on either the amount of sold items, the revenue or the profit.

## setup

The flink services are written in Scala. In order to build the executable jar files, you need java (version 6) and the sbt build tool. Sbt will fetch the required Scala version and necessary dependencies automatically.

Before compiling the services, you might need to adapt some of their settings, i.e. the url of the KAFKA log. To do this, you can update the `application.conf` files in the `resources/` folder of each service's project path.

The next step is to open a console in the cloned repository and enter `sbt assembly`. This command will build the jar files and put them all into the folder `target/jars/`.

There are now two ways to start the processing of these Flink services. Both require an existing installation of Flink in order to work.

The first way is to use the web interface (usually exposed via port 8081) of Flink. Click on `Submit new Job` on the left menu and upload your jar files. Then for each service, select your jar file and click on submit.

The second way is to use the command line. Start a new job just with `./bin/flink run path/to/repository/target/jars/NameOfService.jar`.
