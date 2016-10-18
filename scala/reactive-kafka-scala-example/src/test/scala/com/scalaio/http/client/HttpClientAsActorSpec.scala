package com.scalaio.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.testkit.{TestKit, TestProbe}
import com.scalaio.http.client.actor.HttpClientAsActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class HttpClientAsActorSpec() extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  override def afterAll {
    // helps avoid abrupt system closure
    Http()
      .shutdownAllConnectionPools()
      .onComplete { _ => system.terminate() }
  }

  "An Http actor" must {

    "consume http endpoints" in {
      val worker = TestProbe("worker")

      // when
      system.actorOf(HttpClientAsActor.props(worker.ref), "httpClientActor")

      // then
      val responseMessage = worker.expectMsgType[String](5.seconds)
      responseMessage shouldBe "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"
    }

  }
}
