package com.scalaio.kafka.producer

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

object XebiaConsumerToCommitableSinkProducerMain extends App {

  type KafkaMessage = ProducerRecord[Array[Byte], String]

  implicit val system = ActorSystem("Consumer2ProducerMain")
  implicit val materializer = ActorMaterializer()

  val consumerSettings =
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("Consumer2Producer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings =
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics("topic1"))
    .map { msg =>
      println(s"topic1 -> topic2: $msg")
      ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
        "topic2",
        msg.record.value
      ), msg.committableOffset)
    }
    .runWith(Producer.commitableSink(producerSettings))

}
