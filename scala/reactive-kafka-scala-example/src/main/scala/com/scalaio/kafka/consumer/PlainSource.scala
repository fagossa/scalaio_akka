package com.scalaio.kafka.consumer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.scalaio.kafka.consumer.Settings.consumerSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.Future

object Settings {

  def consumerSettings(implicit system: ActorSystem) =
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("PlainSourceConsumer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
}

object PlainSourceConsumerMain extends App {
  implicit val system = ActorSystem("PlainSourceConsumerMain")
  implicit val materializer = ActorMaterializer()

  // plainSource, don't do auto commit by default!!!, see consumerSettings
  Consumer
    .plainSource(consumerSettings, Subscriptions.topics("topic1"))
    .mapAsync(1)(record => BusinessController.handleMessage(record.value()))
    .runWith(Sink.ignore)
}

object BusinessController {
  type Service[A, B] = A => Future[B]

  val handleMessage: Service[String, String] =
    (message) => Future.successful(message.toUpperCase)

}
