package com.example.tcp

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp.{Connected, ConnectionClosed}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.ByteString
import com.example.tcp.client.ClientActor
import com.example.tcp.server.ServerActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration

class ClientAndServerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ClientAndServerSpec"))

  "Client and server" must {
    "exchange data" in {
      val serverActor = _system.actorOf(ServerActor.props(new InetSocketAddress("localhost", 1040)))

      val listener = TestProbe()
      val clientActor = _system.actorOf(ClientActor.props(new InetSocketAddress("localhost", 1040), listener.ref))

      listener.expectMsgType[Connected](FiniteDuration(1, TimeUnit.SECONDS))

      clientActor ! ByteString("some data\n".getBytes("UTF-8"))

      listener.expectMsg[ByteString](FiniteDuration(1, TimeUnit.SECONDS), ByteString("vrph#gdwd\n".getBytes("UTF-8")))

      clientActor ! "close"

      listener.expectMsg[String](FiniteDuration(1, TimeUnit.SECONDS), "connection closed")
    }
  }
}
