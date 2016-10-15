package com.scalaio.tcp.server

import java.net.InetSocketAddress
import java.nio.charset.Charset

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.{IO, Tcp}

object ServerActor {
  def props(bindAddress: InetSocketAddress) =
    Props(classOf[ServerActor], bindAddress)
}

class ServerActor(bindAddress: InetSocketAddress) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  // TODO: verify why we bind from within the actor
  IO(Tcp) ! Bind(self, bindAddress)

  def receive = {
    case b @ Bound(localAddress) =>
      log.info(s"Tcp Server bound to <$localAddress>")

    case CommandFailed(_: Bind) =>
      log.warning("Tcp ServerActor failed to bind. Stopping...")
      context stop self

    case c @ Connected(remote, local) =>
      log.info(s"Tcp Server Connected. remote=<$remote>, local=<$local>. Starting handler...")
      val handler = context.actorOf(Props(new SimplisticHandler(Charset.forName("UTF-8"))))
      val connection = sender()
      connection ! Register(handler)
  }
}
