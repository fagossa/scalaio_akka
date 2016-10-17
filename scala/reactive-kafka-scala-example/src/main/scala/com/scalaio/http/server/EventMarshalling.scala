package com.scalaio.http.server

import akka.http.scaladsl.unmarshalling.Unmarshaller

case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

case class Error(message: String)

trait EventMarshalling {
  import BoxOffice._
  import akka.http.scaladsl.model.HttpEntity
  import play.api.libs.json.Json

  implicit val eventDescriptionFormat = Json.format[EventDescription]
  implicit val eventFormat = Json.format[Event]
  implicit val eventsFormat = Json.format[Events]
  implicit val ticketRequestFormat = Json.format[TicketRequest]
  implicit val ticketFormat = Json.format[TicketSeller.Ticket]
  implicit val ticketsFormat = Json.format[TicketSeller.Tickets]
  implicit val errorFormat = Json.format[Error]

  implicit val umEventDescription: Unmarshaller[HttpEntity, EventDescription] = {
    Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
      Json.parse(data.toArray).as[EventDescription]
    }
  }

  implicit val umTicketRequest: Unmarshaller[HttpEntity, TicketRequest] = {
    Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
      Json.parse(data.toArray).as[TicketRequest]
    }
  }
}
