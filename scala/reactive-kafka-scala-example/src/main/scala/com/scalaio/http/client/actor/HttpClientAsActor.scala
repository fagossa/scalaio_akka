package com.scalaio.http.client.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.{ByteString, Timeout}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.duration._

class HttpClientAsActor(notifier: ActorRef) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  override def preStart() = {
    http
      .singleRequest(HttpRequest(method = GET, uri = "https://jsonplaceholder.typicode.com/posts/1"))
      .pipeTo(self)
  }

  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      val response: Future[ByteString] = entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      log.info(s"got response $headers $entity")
      response pipeTo self
      context become handlingMessage

    case resp@HttpResponse(code, _, _, _) =>
      log.warning("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

  def handlingMessage: Receive = {
    case content: ByteString =>
      log.info("Success was OK: " + content)
      val contentAsString = (Json.parse(content.utf8String) \ "title").as[String]
      notifier ! contentAsString
      context become receive
  }

}

object HttpClientAsActor {

  def props(notifier: ActorRef) = Props(classOf[HttpClientAsActor], notifier)

}
