package com.scalaio.kafka.producer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ProducerSettings, _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.scalaio.kafka.producer.Settings.{consumerSettings, producerSettings}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

object Settings {

  def consumerSettings(implicit system: ActorSystem) =
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("CommitConsumerToFlowProducer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def producerSettings(implicit system: ActorSystem) =
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")
}

object ProducerMain extends App {
  implicit val system = ActorSystem("CommitConsumerToFlowProducerMain")
  implicit val materializer = ActorMaterializer()

  val names = List("Hugo", "Paco", "Luis")

  // 1. simplest way to produce
  Source(names)
    .map(_.toUpperCase)
    .map { elem => new ProducerRecord[Array[Byte], String]("topic1", elem)}
    .runWith(Producer.plainSink(producerSettings))

  // 2. consume and produce
  Consumer
    .committableSource(consumerSettings, Subscriptions.topics("topic1"))
    .map { msg =>
      println(s"topic1 -> topic2: $msg")
      ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
        "topic2",
        msg.record.value
      ), msg.committableOffset)
    }
    .via(Producer.flow(producerSettings))
    .mapAsync(producerSettings.parallelism) { result =>
      result.message.passThrough.commitScaladsl()
    }
    .runWith(Sink.ignore)
}
