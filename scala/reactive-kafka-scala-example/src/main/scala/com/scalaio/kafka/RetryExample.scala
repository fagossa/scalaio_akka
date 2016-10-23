package com.scalaio.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object RetryExample extends App {
  val logger = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem("RetryExample")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  Source(1 to 5)
    .via(ControllerFlow.apply)
    .runWith(Sink.foreach(v => logger.info(s"without retry: $v")))

  import akka.stream.contrib._
  Source(1 to 5)
    .map(i => (i, i))
    .via(Retry(ControllerFlowWithRetry[Int]) { i => logger.info(s"retrying $i ..."); Some((i, i)) })
    .collect { case (Success(i), j) => i }
    .runWith(Sink.foreach(v => logger.info(s"with retry $v")))

  Await.result(system.terminate(), 5.seconds)
}

object ControllerFlowWithRetry {
  val logger = LoggerFactory.getLogger(getClass)

  def apply[T] = {
    Flow[(Int, T)]
      .map { case (i, j) =>
        val value: Try[Int] = randomFailure(i)
        logger.info(s"result for $i = $value, retry: $j")
        (value, j)
      }
  }

  val random = scala.util.Random
  val randomFailure: Int => Try[Int] =
    (value) =>
      if (random.nextFloat() > 0.4) Success(value)
      else Failure(new IllegalStateException(s"Failure for $value!"))
}

object ControllerFlow {
  def apply: Flow[Int, Try[String], akka.NotUsed]#Repr[Try[String]] = {
    Flow[Int]
      .map(s => s"value = $s")
      .map(randomFailure)
  }

  val random = scala.util.Random
  val randomFailure: String => Try[String] =
    (value) =>
      if (random.nextFloat() > 0.4) Success(value.toUpperCase)
      else Failure(new IllegalStateException(s"Failure for $value!"))
}

