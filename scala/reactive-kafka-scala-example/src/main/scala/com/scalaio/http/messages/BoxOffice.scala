package com.scalaio.http.messages

import akka.actor.{Actor, Props}

object BoxOffice {

  case class CreateEvent(event: String, nrOfTickets: Int)

  case class TicketRequest

  case class EventDescription

  case class EventCreated(event: Any)

  case object EventExists

  case class EventResponse

  val props = Props(classOf[BoxOffice])

  def name: String = "BoxOffice"

}

class BoxOffice extends Actor {

  override def receive: Receive = ???
}

