reactive-kafka-scala-example
=========================
reactive-kafka examples in scala

Akka Streams Kafka: http://doc.akka.io/docs/akka-stream-kafka/current/home.html

### Install Kafka on OSX
```
brew install kafka
```

### Start Kafka
```
zkserver start
kafka-server-start /usr/local/etc/kafka/server.properties
```

### Start a consumer on console for topic1/topic2
```
kafka-console-consumer --zookeeper localhost:2181 --topic topic1  --from-beginning
kafka-console-consumer --zookeeper localhost:2181 --topic topic2  --from-beginning
```

### Start a producer on console for topic1
```
kafka-console-producer --broker-list localhost:9092 --topic topic1
```
===
### Start reactive-kafka examples
```
git clone https://github.com/makersu/reactive-kafka-scala-example.git
cd reactive-kafka-scala-example
sbt run
```
```
Multiple main classes detected, select one to run:

 [1] com.scalaio.kafka.consumer.BatchCommittableSourceConsumerMain
 [2] com.scalaio.kafka.consumer.CommittableSourceConsumerMain
 [3] com.scalaio.kafka.consumer.PlainSourceConsumerMain
 [4] com.scalaio.kafka.producer.CommitConsumerToFlowProducerMain
 [5] com.scalaio.kafka.producer.ConsumerToCommitableSinkProducerMain
 [6] com.scalaio.kafka.producer.FlowProducerMain
 [7] com.scalaio.kafka.producer.PlainSinkProducerMain

Enter number:
```



