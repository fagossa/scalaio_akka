package com.scalaio.http.client.slack

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest}
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.util.ByteString
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.concurrent.duration._

object LogAlerter {

  lazy val logger = LoggerFactory.getLogger(getClass)
/*
  val readEvent: (String) => Option[OutgoingLogEvent] = {
    case LogEventFromJson(event) =>
      Some(OutgoingLogAdapter(event).toOutgoing)
    case message: String =>
      None
  }

  def groupErrorsAndPrepareSlackMessages(slackConfig: Config, kibanaConfig: Config)(implicit mat: ActorMaterializer): Flow[String, HttpRequest, akka.NotUsed] = {
    val webhookUri = slackConfig.getString("webhookuri")
    val channel = slackConfig.getString("channel")
    val kibana = kibanaConfig.getString("connect")
    val events_grouped_by = slackConfig.getInt("events_grouped_by")
    val events_grouped_within = slackConfig.getDuration("events_grouped_within")
    val throttle_one_slack_request_per = slackConfig.getDuration("throttle_one_slack_request_per")

    Flow[String].map(LogAlerter.readEvent)
      .collect { case Some(event) => event}
      .filter(_.level == "err")
      .groupedWithin(events_grouped_by, events_grouped_within.toMillis.milliseconds)
      .throttle(elements = 1, maximumBurst = 1, per = throttle_one_slack_request_per.toMillis.milliseconds, mode = ThrottleMode.Shaping)
      .log("throttled", _.size)
      .collect { case events if events.nonEmpty => events}
      .map(toSlackMessage(webhookUri, channel, kibana))
  }

  def toSlackMessage(webhookUri: String, channel: String, kibana: String)(events: Seq[OutgoingLogEvent]) = {
    val eventsByModule: Map[String, Seq[OutgoingLogEvent]] = events.groupBy(_.module).mapValues(_.sortBy(_.timestamp))

    val attachments = eventsByModule.toList.map {
      case (module, moduleErrors) =>
        val fromTimestamp = moduleErrors.map(_.timestamp).min
        val toTimestamp = moduleErrors.map(_.timestamp).max
        Json.obj(
          "color" -> "danger",
          "fallback" -> s"$module: ${moduleErrors.size} errors detected in logs",
          "title" -> s"$module: ${moduleErrors.size} errors detected in logs",
          "title_link" -> s"$kibana/#/discover?_g=(time:(from:'$fromTimestamp',mode:absolute,to:'$toTimestamp'))&_a=(columns:!(message),filters:!((meta:(disabled:!f,index:'ingenico-app-logs-*',key:module,negate:!f,value:$module),query:(match:(module:(query:$module,type:phrase))))),index:'ingenico-app-logs-*',interval:auto,query:'',sort:!(timestamp,desc))",
          "text" -> moduleErrors.map(error => s"${error.timestamp}: ${error.message}").mkString("\n")
        )
    }

    HttpRequest(HttpMethods.POST, webhookUri, headers = List.empty,
      entity = HttpEntity.Strict(`application/json`, ByteString(Json.obj(
        "text" -> s"${events.size} errors detected in logs",
        "channel" -> channel,
        "attachments" -> attachments).toString(), "UTF-8")
      )
    )
  }*/

}
