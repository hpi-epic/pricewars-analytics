lazy val root = ( project in file(".") )
   .aggregate(`pricewars-utils`, flinkUtils, merchantStatistics, marketshare, aggregatedProfit)

lazy val `pricewars-utils` = project in file("utils")

val versions = new {
  def flink = "1.1.3"
  def json = "3.5.0"
  def jodaTime = "2.9.6"
}

lazy val flinkUtils = (project in file("flinkUtils")).settings(
  name := "flinkUtils",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.json4s" %% "json4s-native" % versions.json,
    "org.json4s" %% "json4s-jackson" % versions.json,
    "joda-time" % "joda-time" % versions.jodaTime,
    "org.json4s" %% "json4s-ext" % versions.json,
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided")
).dependsOn(`pricewars-utils`)

lazy val merchantStatistics = (project in file("merchantStatistics"))
    .dependsOn(`pricewars-utils`)

lazy val marketshare = (project in file("marketshare"))
    .dependsOn(`pricewars-utils`)

lazy val aggregatedProfit = (project in file("aggregatedProfit")).settings(
  name := "aggregatedProfit",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink)
).dependsOn(flinkUtils)