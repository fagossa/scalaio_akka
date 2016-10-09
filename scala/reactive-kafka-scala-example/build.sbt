name := "scala_io_examples"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.11-RC1",
  "io.spray"          %% "spray-json"      % "1.3.1",
  // logs
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback"    %  "logback-classic" % "1.1.3",
  // test
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test")


fork in run := true