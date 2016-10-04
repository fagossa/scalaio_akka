package com.example.tcp.client

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ClientActorSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("Tcp ClientActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A ClientActor" must {
    "Connect to local test server" in {
      pending
    }
  }

}
