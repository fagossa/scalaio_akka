package com.scalaio.tcp.server

import java.nio.charset.Charset

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp
import akka.util.ByteString

object SimplisticHandler {
  def props(encoding: Charset) = Props(classOf[SimplisticHandler], encoding)
}

class SimplisticHandler(encoding: Charset) extends Actor with ActorLogging {
  import Tcp._

  def receive: Receive = {
    case Received(data) =>
      log.info(s"SimplisticHandler received <${data.length}> bytes")
      sender() ! Write(aResponsePacketFrom(data))

    case PeerClosed =>
      log.info("SimplisticHandler received PeerClosed, stopping.")
      context stop self
  }

  def aResponsePacketFrom(data: ByteString): ByteString = {
    val map = new String(data.toArray, 0, data.length, encoding).flatMap(caesarCypher)
    ByteString(map.getBytes(encoding))
  }

  def caesarCypher(c: Char): scala.collection.mutable.ArrayOps[Char] =
    if (c != '\n' && c != '\r') Character.toChars(c + 3) else Array(c)

}
