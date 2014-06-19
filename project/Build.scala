import sbt._
import sbt.Keys._
import sbtassembly.Plugin.assemblySettings
import sbtassembly.Plugin.AssemblyKeys._


object Resolvers {
  val customResolvers = Seq(
    "SoftwareMill Public Releases" at "https://nexus.softwaremill.com/content/repositories/releases/",
    "SoftwareMill Public Snapshots" at "https://nexus.softwaremill.com/content/repositories/snapshots/",
    "spray" at "http://repo.spray.io/"
  )
}

object Dependencies {

  private val slf4jVersion = "1.7.6"
  val logging = Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.1",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  )

  val macwireVersion = "0.6"
  val macwire = Seq(
    "com.softwaremill.macwire" %% "macros" % macwireVersion,
    "com.softwaremill.macwire" %% "runtime" % macwireVersion
  )

  val json4sVersion = "3.2.10"
  val json4s = "org.json4s" %% "json4s-jackson" % json4sVersion
  val json4sExt = "org.json4s" %% "json4s-ext" % json4sVersion

  val sprayVersion = "1.3.1-20140423"
  val spray = Seq(
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-testkit" % sprayVersion % "test"
  )

  val httpStack = Seq(
    json4s,
    json4sExt
  ) ++ spray

  val gardenVersion = "0.0.20-SNAPSHOT"
  val garden = Seq("garden-web", "lawn", "shrubs").map("com.softwaremill.thegarden" %% _ % gardenVersion)

  val akkaVersion = "2.3.3"
  val akkaActors = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val akka = Seq(akkaActors, akkaTestKit)

  val h2 = "com.h2database" % "h2" % "1.3.175"
  val c3p0 = "com.mchange" % "c3p0" % "0.9.5-pre6"

  val slick = "com.typesafe.slick" %% "slick" % "2.1.0-M2"
  val flyway = "com.googlecode.flyway" % "flyway-core" % "2.3"

  val dbStack = Seq(h2, c3p0, slick, flyway)

  lazy val commonDependencies = logging ++ macwire ++ httpStack ++ akka ++ dbStack ++ garden
}

object VoteCounterBuild extends Build {

  import Dependencies._

  override val settings = super.settings ++ Seq(
    name := "vote-counter",
    version := "1.0",
    scalaVersion := "2.11.1",
    scalacOptions in GlobalScope in Compile := Seq("-unchecked", "-deprecation", "-feature"),
    scalacOptions in GlobalScope in Test := Seq("-unchecked", "-deprecation", "-feature"),
    organization := "com.softwaremill.votecounter"
  )

  lazy val slf4jExclusionHack = Seq(
    ivyXML :=
      <dependencies>
        <exclude org="org.slf4j" artifact="slf4j-log4j12"/>
        <exclude org="log4j" artifact="log4j"/>
      </dependencies>
  )

  lazy val commonSettings = Defaults.defaultSettings ++
    Seq(isSnapshot <<= isSnapshot or version(_ endsWith "-SNAPSHOT")) ++ slf4jExclusionHack ++
    Seq(
      resolvers ++= Resolvers.customResolvers
    ) ++
    Seq(
      mainClass in assembly := Some("com.softwaremill.votecounter.web.VoteCounterWeb")
    ) ++ assemblySettings


  lazy val root = Project(
    id = "vote-counter",
    base = file("."),
    settings = commonSettings
  ).settings(
      libraryDependencies ++= commonDependencies,
      // HACK
      /*
       * I couldn't get this directory to be mapped to webapp, so we have a structure web/webapp
       * Here are some links where to start researching this topic:
       * http://www.scala-sbt.org/release/docs/Howto/defaultpaths.html
       * http://www.scala-sbt.org/release/docs/Detailed-Topics/Paths.html
       * http://www.scala-sbt.org/release/docs/Detailed-Topics/Mapping-Files.html
       */
      unmanagedResourceDirectories in Compile <++= baseDirectory {
        base =>
          Seq(base / "web")
      }
//      webappResources in Compile := Seq(baseDirectory.value / "web" / "webapp")
    )
}
