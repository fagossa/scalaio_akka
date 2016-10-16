package com.scalaio.tcp.client

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress

object ClientActor {
  def props(remote: InetSocketAddress, tcp: ActorRef, replies: ActorRef) =
    Props(classOf[ClientActor], remote, tcp, replies)
}

class ClientActor(remote: InetSocketAddress, tcp:ActorRef, listener: ActorRef) extends Actor {
  import Tcp._
  import context.system

  tcp ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}
