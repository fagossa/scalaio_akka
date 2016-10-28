package com.scalaio.tcp.server

import java.net.InetSocketAddress
import java.nio.charset.Charset

import akka.actor.ActorSystem
import akka.io.Tcp._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class ServerActorSpec  (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("TcpServerActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A ServerActor" must {
    "accept connection and reply to client" in {
      val listeningAdress = new InetSocketAddress("localhost", 1040)
      val fakeRemoteAddress = new InetSocketAddress("localhost", 1042)
      val tcpProbe = TestProbe()
      val deathWatchProbe = TestProbe()

      val handlerRef = _system.actorOf(SimplisticHandler.props(Charset.forName("UTF-8")))
      val serverActor = _system.actorOf(ServerActor.props(listeningAdress, tcpProbe.ref, handlerRef))

      deathWatchProbe.watch(handlerRef)

      tcpProbe.expectMsg(Bind(serverActor, listeningAdress))

      tcpProbe.send(serverActor, Bound(listeningAdress))

      tcpProbe.send(serverActor, Connected(fakeRemoteAddress, listeningAdress))

      tcpProbe.expectMsgPF(5.seconds, "fishing ServerActor's Register") {
        case Register(`handlerRef`, _, _) => true
        case _ => false
      }

      val data = ByteString("some data\n".getBytes("UTF-8"))
      tcpProbe.send(handlerRef, Received(data))

      val serverResponse = ByteString("vrph#gdwd\n".getBytes("UTF-8"))
      tcpProbe.expectMsg(Write(serverResponse))

      tcpProbe.send(handlerRef, PeerClosed)

      deathWatchProbe.expectTerminated(handlerRef, 5.seconds)
    }
  }

}
