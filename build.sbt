lazy val root = ( project in file(".") )
   .aggregate(`pricewars-utils`, merchantStatistics)

lazy val `pricewars-utils` = project in file("utils")

lazy val merchantStatistics = (project in file("merchantStatistics"))
    .dependsOn(`pricewars-utils`)