package com.scalaio.tcp.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp

object ServerActor {
  def props(bindAddress: InetSocketAddress, tcp: ActorRef, handler:ActorRef) =
    Props(classOf[ServerActor], bindAddress, tcp, handler)
}

class ServerActor(bindAddress: InetSocketAddress, tcp: ActorRef, handler: ActorRef) extends Actor with ActorLogging {
  import Tcp._

  // TODO: verify why we bind from within the actor
 tcp ! Bind(self, bindAddress)

  def receive = {
    case b @ Bound(localAddress) =>
      log.info(s"Tcp Server bound to <$localAddress>")

    case CommandFailed(_: Bind) =>
      log.warning("Tcp ServerActor failed to bind. Stopping...")
      context stop self

    case c @ Connected(remote, local) =>
      log.info(s"Tcp Server Connected. remote=<$remote>, local=<$local>. Registering handler...")
      val connection = sender()
      connection ! Register(handler)
  }
}
