package com.scalaio.kafka.consumer

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.Future

object CommittableSourceConsumerMain extends App {

  type KafkaMessage = CommittableMessage[Array[Byte], String]

  implicit val system = ActorSystem("CommittableSourceConsumerMain")
  implicit val materializer = ActorMaterializer()

  val consumerSettings =
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("CommittableSourceConsumer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics("topic1"))
    .mapAsync(1) { msg =>
      BusinessController.handleMessage(msg.record.value())
        .flatMap(response => msg.committableOffset.commitScaladsl())
        .recoverWith { case e => msg.committableOffset.commitScaladsl() }
    }
    .runWith(Sink.ignore)

}

object BusinessController {

  type Service[A, B] = A => Future[B]

  val handleMessage: Service[String, String] =
    (message) => Future.successful(message.toUpperCase)

}
