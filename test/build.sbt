name := "test"

version := "1.0"

scalaVersion := "2.11.8"

//val flinkV = "1.2-SNAPSHOT"
val flinkV = "1.1.3"

resolvers += "Apache Snapshots" at "http://repository.apache.org/snapshots/"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala" % flinkV,
  "org.apache.flink" %% "flink-streaming-scala" % flinkV,
  "org.apache.flink" %% "flink-clients" % flinkV,
  "org.apache.flink" %% "flink-connector-kafka-0.9" % flinkV,
  "org.json4s" %% "json4s-native" % "3.5.0",
  "joda-time" % "joda-time" % "2.9.6")
