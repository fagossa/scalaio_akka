package com.scalaio.http.monitoring

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import com.typesafe.config.Config
import com.yammer.metrics.HealthChecks
import com.yammer.metrics.core.HealthCheckRegistry
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, Json}

import scala.util.{Failure, Success}
import scala.collection.convert.wrapAsScala._

object MonitoringServer {

  lazy val logger = LoggerFactory.getLogger(getClass)

  def handleHealthchecks(registry: HealthCheckRegistry = HealthChecks.defaultRegistry()): Route = {
    path("health") {
      get {
        complete {
          val checks = registry.runHealthChecks
          val payload = JsArray(checks.map {
            case (name, result) =>
              Json.obj(
                "name" -> name,
                "healthy" -> result.isHealthy,
                "message" -> result.getMessage
              )
          }.toSeq)

          val status = if (checks.values().forall(_.isHealthy)) OK else InternalServerError

          HttpResponse(entity = HttpEntity(`application/json`, Json.stringify(payload)), status = status)
        }
      }
    }
  }

  def start(serverConfig: Config, registry: HealthCheckRegistry = HealthChecks.defaultRegistry())(implicit system: ActorSystem, materializer: Materializer): Unit = {

    val host = serverConfig.getString("host")
    val port = serverConfig.getInt("port")

    logger.info(s"Starting monitoring server at: $host:$port")

    val routes = handleHealthchecks(registry) ~ redirect(Uri("/health"), StatusCodes.SeeOther)

    import system.dispatcher
    Http().bindAndHandle(routes, host, port).onComplete {
      case Success(Http.ServerBinding(address)) =>
        logger.info(s"Monitoring server started at :$address")

      case Failure(t) =>
        logger.error("Error while trying to start monitoring server", t)
    }


  }
}