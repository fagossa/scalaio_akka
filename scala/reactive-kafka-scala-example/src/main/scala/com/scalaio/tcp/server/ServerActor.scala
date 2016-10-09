package com.scalaio.tcp.server

import java.net.InetSocketAddress
import java.nio.charset.Charset

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

object ServerActor {
  def props(bindAddress: InetSocketAddress) =
    Props(classOf[ServerActor], bindAddress)
}

class ServerActor(bindAddress: InetSocketAddress) extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, bindAddress)

  def receive = {
    case b @ Bound(localAddress) =>
      println(s"Tcp Server bound to $localAddress")

    case CommandFailed(_: Bind) =>
      println("Tcp ServerActor failed to bind. Stopping...")
      context stop self

    case c @ Connected(remote, local) =>
      println(s"Tcp ServerActor Connected. remote=$remote, local=$local. Starting handler...")
      val handler = context.actorOf(Props(new SimplisticHandler(Charset.forName("UTF-8"))))
      val connection = sender()
      connection ! Register(handler)
  }
}
