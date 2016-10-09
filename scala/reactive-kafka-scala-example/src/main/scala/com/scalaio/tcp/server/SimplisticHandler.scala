package com.scalaio.tcp.server

import java.nio.charset.Charset

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString

class SimplisticHandler(encoding: Charset) extends Actor {
  import Tcp._
  def receive = {
    case Received(data) =>
      println(s"SimplisticHandler received ${data.length} bytes")
      sender() ! Write(ByteString(new String(data.toArray, 0, data.length, encoding).flatMap { c =>
        if(c != '\n' && c != '\r') Character.toChars(c + 3) else Array(c)
      }.getBytes(encoding)))
    case PeerClosed =>
      println("SimplisticHandler received PeerClosed, stopping.")
      context stop self
  }
}
