lazy val root = ( project in file(".") )
   .aggregate(`pricewars-utils`, merchantStatistics, marketshare)

lazy val `pricewars-utils` = project in file("utils")

lazy val merchantStatistics = (project in file("merchantStatistics"))
    .dependsOn(`pricewars-utils`)

lazy val marketshare = (project in file("marketshare"))
    .dependsOn(`pricewars-utils`)