lazy val analytics = ( project in file(".") )
   .aggregate(common, aggregatedProfit, cumulativeProfit, cumulativeMarketShare, holdingCost)

val versions = new {
  def flink = "1.6.0"
  def json = "3.5.0"
  def jodaTime = "2.9.6"
}

lazy val common = (project in file("common")).settings(
  name := "common",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.json4s" %% "json4s-native" % versions.json,
    "org.json4s" %% "json4s-jackson" % versions.json,
    "joda-time" % "joda-time" % versions.jodaTime,
    "com.typesafe" % "config" % "1.3.1",
    "org.json4s" %% "json4s-ext" % versions.json,
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided")
)

lazy val cumulativeProfit = (project in file("cumulativeProfit")).settings(
  name := "cumulativeProfit",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink
  ),
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
  assemblyOutputPath in assembly := file(s"./target/jars/${name.value}.jar")
).dependsOn(common)

lazy val cumulativeMarketShare = (project in file("cumulativeMarketShare")).settings(
  name := "cumulativeMarketShare",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink
  ),
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
  assemblyOutputPath in assembly := file(s"./target/jars/${name.value}.jar")
).dependsOn(common)

lazy val aggregatedProfit = (project in file("aggregatedProfit")).settings(
  name := "aggregatedProfit",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink),
  assemblyOutputPath in assembly := file(s"./target/jars/${name.value}.jar")
).dependsOn(common)

lazy val holdingCost = (project in file("holdingCost")).settings(
  name := "holdingCost",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink),
  assemblyOutputPath in assembly := file(s"./target/jars/${name.value}.jar")
).dependsOn(common)