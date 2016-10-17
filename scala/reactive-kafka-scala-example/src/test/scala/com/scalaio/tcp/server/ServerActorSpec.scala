package com.scalaio.tcp.server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ServerActorSpec  (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("TcpServerActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A ServerActor" must {
    "accept connection and reply to client" in {
      pending
    }
  }

}
