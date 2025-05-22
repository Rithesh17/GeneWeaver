ThisBuild / scalaVersion     := "2.13.14"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "edu.ucsc.geneweaver"

val chiselVersion = "3.6.1"

lazy val root = (project in file("."))
  .settings(
    name := "GeneWeaver",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.6.2" % Test,
      "org.scalatest" %% "scalatest" % "3.2.15" % Test
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit"
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)
  )
