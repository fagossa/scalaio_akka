package com.scalaio.tcp.client

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.io.Tcp
import akka.io.Tcp.{ConnectionClosed, _}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration

class ClientActorSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("TcpClientActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A ClientActor" must {
    "Connect to local test server" in {
      val targetAdress = new InetSocketAddress("localhost", 1040)
      val fakeLocalAddress = new InetSocketAddress("localhost", 1042)
      val tcpProbe = TestProbe()
      val listenerProbe = TestProbe()
      val deathWatchProbe = TestProbe()
      val clientActor = _system.actorOf(ClientActor.props(new InetSocketAddress("localhost", 1040), tcpProbe.ref , listenerProbe.ref))

      deathWatchProbe.watch(clientActor)

      tcpProbe.expectMsg(Connect(targetAdress))

      tcpProbe.send(clientActor, Connected(targetAdress, fakeLocalAddress))

      listenerProbe.expectMsg(Connected(targetAdress, fakeLocalAddress))

      tcpProbe.expectMsg(Register(clientActor))

      val data = ByteString("some data\n".getBytes("UTF-8"))
      clientActor ! data

      tcpProbe.expectMsg(Write(data))

      //TODO tcpProbe should send Received(transformedData) to simulate server response
      //Simulate server response
      val serverResponse = ByteString("vrph#gdwd\n".getBytes("UTF-8"))
      tcpProbe.send(clientActor, Received(serverResponse))

      listenerProbe.expectMsg[ByteString](serverResponse)

      clientActor ! "close"

      tcpProbe.expectMsg(Close)

      tcpProbe.send(clientActor, Closed)

      deathWatchProbe.expectTerminated(clientActor, FiniteDuration(2, TimeUnit.SECONDS))
    }
  }

}
