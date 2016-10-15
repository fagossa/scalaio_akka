package com.scalaio.tcp.server

import java.nio.charset.Charset

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp
import akka.util.ByteString

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
    val map = new String(data.toArray, 0, data.length, encoding).flatMap(transformString)
    ByteString(map.getBytes(encoding))
  }

  // TODO: rename this function!
  // I din't get what it does :(
  def transformString(c: Char): scala.collection.mutable.ArrayOps[Char] =
    if (c != '\n' && c != '\r') Character.toChars(c + 3) else Array(c)

}
