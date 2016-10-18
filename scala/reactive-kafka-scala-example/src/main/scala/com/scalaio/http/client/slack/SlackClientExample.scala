package com.scalaio.http.client.slack

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.scalaio.http.client.slack.BusinessController.{sinkValue, transformValue}
import org.slf4j.LoggerFactory

object SlackClientExample extends App {

  implicit val system = ActorSystem("SlackClientExample")
  implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  Source(List("Hugo", "Paco", "Luis"))
    .map(transformValue)
    .runWith(Sink.foreach(sinkValue))

  system.terminate()

}

object BusinessController {

  val logger = LoggerFactory.getLogger(getClass)

  def sinkValue(value: String) = logger.info(s"$value")

  def transformValue(name: String) = name.toUpperCase
}