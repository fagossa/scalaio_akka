package com.scalaio.http

import scala.concurrent.{ExecutionContext, Future}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.scalaio.http.TicketSeller.Tickets
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json._

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes with EventMarshalling {

  def log: Logger = LoggerFactory.getLogger(getClass)

  implicit val requestTimeout = timeout
  implicit def executionContext = system.dispatcher

  def createBoxOffice() = system.actorOf(BoxOffice.props, BoxOffice.name)

}

trait RestRoutes extends BoxOfficeApi
  with EventMarshalling {
  import StatusCodes._

  def routes: Route = eventsRoute ~ eventRoute ~ ticketsRoute

  def eventsRoute =
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          // GET /events
          onSuccess(getEvents) { events =>
            complete(OK, stringify(toJson(events)))
          }
        }
      }
    }

  def eventRoute =
    pathPrefix("events" / Segment) { event =>
      pathEndOrSingleSlash {
        post {
          // POST /events/:event
          entity(as[EventDescription]) { ed =>
            onSuccess(createEvent(event, ed.tickets)) {
              case BoxOffice.EventCreated(event) => complete(Created, stringify(toJson(event)))
              case BoxOffice.EventExists =>
                val err = Error(s"$event event exists already.")
                complete(BadRequest, stringify(toJson(err)))
            }
          }
        } ~
          get {
            // GET /events/:event
            onSuccess(getEvent(event)) {
              _.fold(complete(NotFound))(e => complete(OK, stringify(toJson(e))))
            }
          } ~
          delete {
            // DELETE /events/:event
            onSuccess(cancelEvent(event)) {
              _.fold(complete(NotFound))(e => complete(OK, stringify(toJson(e))))
            }
          }
      }
    }

  def ticketsRoute =
    pathPrefix("events" / Segment / "tickets") { event =>
      post {
        pathEndOrSingleSlash {
          // POST /events/:event/tickets
          entity(as[TicketRequest]) { request =>
            onSuccess(requestTickets(event, request.tickets)) { tickets =>
              if(tickets.entries.isEmpty) complete(NotFound)
              else complete(Created, stringify(toJson(tickets)))
            }
          }
        }
      }
    }

}

trait BoxOfficeApi {
  import BoxOffice._
  def log: Logger
  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val boxOffice = createBoxOffice()

  def createEvent(event: String, nrOfTickets: Int) = {
    log.info(s"Received new event $event, sending to $boxOffice")
    boxOffice.ask(CreateEvent(event, nrOfTickets))
      .mapTo[EventResponse]
  }

  def getEvents: Future[Events] =
    boxOffice.ask(GetEvents).mapTo[Events]

  def getEvent(event: String): Future[Option[Event]] =
    boxOffice.ask(GetEvent(event))
      .mapTo[Option[Event]]

  def cancelEvent(event: String): Future[Option[Event]] =
    boxOffice.ask(CancelEvent(event))
      .mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int): Future[Tickets] =
    boxOffice.ask(GetTickets(event, tickets))
      .mapTo[TicketSeller.Tickets]
}


