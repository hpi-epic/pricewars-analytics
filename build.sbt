lazy val root = ( project in file(".") )
   .aggregate(`pricewars-utils`, flinkUtils, aggregatedProfit, cumulativeProfit, cumulativeMarketShare)

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
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided"),
).dependsOn(`pricewars-utils`)

lazy val cumulativeProfit = (project in file("cumulativeProfit")).settings(
  name := "cumulativeProfit",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink
  ),
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
).dependsOn(flinkUtils)

lazy val cumulativeMarketShare = (project in file("cumulativeMarketShare")).settings(
  name := "cumulativeMarketShare",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink
  ),
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
).dependsOn(flinkUtils)

lazy val aggregatedProfit = (project in file("aggregatedProfit")).settings(
  name := "aggregatedProfit",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink),
  assemblyOutputPath in assembly := file(s"./target/jars/${name.value}.jar")
).dependsOn(flinkUtils)
