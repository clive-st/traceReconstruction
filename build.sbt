import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.testproject",
      scalaVersion := "2.12.5",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Traza",

    libraryDependencies ++= Seq(
      scalaTest % Test
        exclude("org.scala-lang", "scala-reflect")
        exclude("org.scala-lang.modules", "scala-xml_2.11"),
      "org.json4s" %% "json4s-native" % "3.6.0-M3",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
      "org.typelevel" %% "cats-core" % "1.1.0",
      "io.monix" %% "monix" % "3.0.0-RC1",
      "com.github.scopt" %% "scopt" % "3.7.0"
    )

  )
