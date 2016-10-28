package com.scalaio.kafka

import akka.actor.ActorSystem
import akka.stream.contrib._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.scalaio.kafka.ControllerFlowWithRetry.{IntWrapper, handleRetry}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object BetterRetryExample extends App {
  val logger = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("RetryExample")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  Source(1 to 5)
    .map(i => (i, IntWrapper(i)))
    .via(Retry(ControllerFlowWithRetry[IntWrapper]())(handleRetry))
    .collect { case (Success(i), j) => i }
    .runWith(Sink.foreach(v => logger.info(s"we got: <$v>")))

  Await.result(system.terminate(), 5.seconds)
}

object ControllerFlowWithRetry {
  val logger = LoggerFactory.getLogger(getClass)

  case class IntWrapper(value: Int, retry: Int = 3) {
    def dropRetry: Option[IntWrapper] = if (retry - 1 >= 0) Some(IntWrapper(value, retry - 1)) else None
  }

  def handleRetry(j: IntWrapper): Option[(Int, IntWrapper)] = {
    logger.info(s"retrying $j ...")
    j.dropRetry.map(retry => (retry.value, retry))
  }

  def apply[T]() = {
    Flow[(Int, T)]
      .map { case (i, j) =>
        val value: Try[Int] = randomFailure(i)
        logger.info(s"result for $i = $value, retry: <$j>")
        (value, j)
      }
  }

  val random = scala.util.Random
  val randomFailure: Int => Try[Int] =
    (value) =>
      if (random.nextFloat() <= 0.7) Failure(new IllegalStateException(s"Failure for $value!"))
      else Success(value)
}

object RetryExample extends App {
  val logger = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("RetryExample")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system))

  Source(1 to 5)
    .via(ControllerFlow())
    .collect { case Success(i) => i }
    .runWith(Sink.foreach(v => logger.info(s"we got: $v")))

  Await.result(system.terminate(), 5.seconds)
}

object ControllerFlow {
  def apply(): Flow[Int, Try[String], akka.NotUsed]#Repr[Try[String]] = {
    Flow[Int]
      .map(s => s"value for <$s>")
      .map(randomFailure)
  }

  val random = scala.util.Random
  val randomFailure: String => Try[String] =
    (value) =>
      if (random.nextFloat() <= 0.4)
        Failure(new IllegalStateException(s"Failure for $value!"))
      else Success(value.toUpperCase)
}

