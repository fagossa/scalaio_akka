name := "scala_io_examples"

version := "1.0"

val akkaVersion = "2.4.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.11-RC1",
  // mapping
  "com.typesafe.play" %% "play-json" % "2.5.8",
  // monitoring
  "com.yammer.metrics" % "metrics-core" % "2.2.0",
  // logs
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback"    %  "logback-classic" % "1.1.3",
  // test
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"

)

// Assembly settings
// mainClass in Global := Some("com.scalaio.http.FrontendMain")

fork in run := true