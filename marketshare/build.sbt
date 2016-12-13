import sbt.Keys._

val versions = new {
  def flink = "1.1.3"
  def json = "3.5.0"
  def jodaTime = "2.9.6"
}

lazy val `pricewars-utils` = project in file("../utils")

lazy val marketshare = (project in file(".")).settings(
  name := "marketshare",
  version := "1.0",
  scalaVersion := "2.11.8",
  resolvers += "Apache Snapshots" at "http://repository.apache.org/snapshots/",
  libraryDependencies ++= Seq(
    "org.apache.flink" %% "flink-scala" % versions.flink,
    "org.apache.flink" %% "flink-streaming-scala" % versions.flink,
    "org.apache.flink" %% "flink-clients" % versions.flink % "provided",
    "org.apache.flink" %% "flink-connector-kafka-0.9" % versions.flink,
    "org.apache.flink" %% "flink-streaming-contrib" % versions.flink % "provided",
    "org.json4s" %% "json4s-native" % versions.json,
    "org.json4s" %% "json4s-jackson" % versions.json,
    "joda-time" % "joda-time" % versions.jodaTime,
    "org.json4s" %% "json4s-ext" % versions.json,
    "org.scalactic" %% "scalactic" % "3.0.1",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test")
).dependsOn(`pricewars-utils`)