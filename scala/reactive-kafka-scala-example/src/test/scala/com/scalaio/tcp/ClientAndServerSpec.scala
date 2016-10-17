package com.scalaio.tcp

import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.io.Tcp.Connected
import akka.io.{IO, Tcp}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.ByteString
import com.scalaio.tcp.client.ClientActor
import com.scalaio.tcp.server.{ServerActor, SimplisticHandler}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration

class ClientAndServerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ClientAndServerSpec"))

  "Client and server" must {
    "exchange data" in {
      val handlerRef = _system.actorOf(SimplisticHandler.props(Charset.forName("UTF-8")))

      val serverActor = _system.actorOf(ServerActor.props(new InetSocketAddress("localhost", 1040), IO(Tcp), handlerRef))

      val listener = TestProbe()
      val clientActor = _system.actorOf(ClientActor.props(new InetSocketAddress("localhost", 1040), IO(Tcp) ,listener.ref))

      listener.expectMsgType[Connected](FiniteDuration(1, TimeUnit.SECONDS))

      clientActor ! ByteString("some data\n".getBytes("UTF-8"))

      listener.expectMsg[ByteString](FiniteDuration(1, TimeUnit.SECONDS), ByteString("vrph#gdwd\n".getBytes("UTF-8")))

      clientActor ! "close"

      listener.expectMsg[String](FiniteDuration(1, TimeUnit.SECONDS), "connection closed")
    }
  }
}
