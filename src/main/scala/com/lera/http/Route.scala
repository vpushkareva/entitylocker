package com.lera.http

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.lera.cache.TTLCache
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * TTL Cache web service API.
 *
 * Requests avaliable:
 * 1. get /healthcheck
 * GET request, provides information if service is alive.
 * Example: curl -v http://localhost:8080/healthcheck
 *
 * 2. get /cache/<key>
 * GET request, returns a value for a particular key.
 * Parameter: <key> : String key
 * Returns:
 * if cache contains key: 200 OK. "value" : <value>
 * if cache not contains key: 204 No Content
 * Example: curl -v http://localhost:8080/cache/47 -X GET
 *
 * 3. post /cache/<key>
 * POST request, puts a value for a particular key in cache.
 * Parameter: <key> : String key. Key length should be less than max key length
 * Request body: value:<value> : String value. Value length should be less than max value length
 * Returns:
 * if key-value pair inserted correctly: 200 OK. "value" : <value>
 * if key-value pair not inserted correctly: 400 Bad Request
 * Example:  curl -d "value=value22333332" -X POST http://localhost:8080/cache/47
 *
 * 4. delete /cache/<key>
 * DELETE request, deletes a key-value pair in a cache.
 * Parameter: <key> : String key
 * Returns:
 * if cache contains key: 200 OK (after deleting)
 * if cache not contains key: 204 No Content
 * Example: curl -v http://localhost:8080/cache/47 -X DELETE
 *
 */
object Route {
  private val config = ConfigFactory.load("application.conf")
  private val maxKeyLength = config.getInt("cache.maxKeyLength")
  private val maxValueLength = config.getInt("cache.maxValueLength")
  private val maxTtlLength = config.getInt("cache.maxTtlLength")

  private val cache = TTLCache.apply[String, String](maxTtlLength)

  implicit val system: ActorSystem = ActorSystem("route")
  implicit val log: LoggingAdapter = Logging(system, "main")

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/healthcheck"), _, _, _) =>
      healthCheckGet()

    case HttpRequest(GET, Uri.Path(s"/cache/$key"), _, _, _) =>
      cacheGet(key)

    case HttpRequest(POST, Uri.Path(s"/cache/$key"), _, entity: HttpEntity, _) =>
      cachePost(key, entity)

    case HttpRequest(DELETE, Uri.Path(s"/cache/$key"), _, _, _) =>
      cacheDelete(key)
  }

  private def healthCheckGet(): HttpResponse = {
    log.info("Healthcheck GET request.")

    HttpResponse(
      StatusCodes.OK
    )
  }

  private def cacheGet(key: String): HttpResponse = {
    log.info(s"Cache GET request with key: $key")

    if (cache.contains(key)) {
      HttpResponse(
        StatusCodes.OK,
        entity = "\"value\" : " + cache.get(key).get
      )
    } else {
      HttpResponse(
        StatusCodes.NoContent
      )
    }
  }

  private def cachePost(key: String, entity: HttpEntity): HttpResponse = {
    val chunk = Unmarshal(entity).to[String]
    val value = Await.result(chunk, 1.second)
    val splitValue = value.split("=")

    log.info(s"Cache POST request with key: $key and value: $value")

    if (splitValue.length == 2 && (key.length < maxKeyLength) && (splitValue(1).length < maxValueLength)) {
      cache.set(key, splitValue(1))
      HttpResponse(
        StatusCodes.OK
      )
    } else {
      HttpResponse(
        StatusCodes.BadRequest
      )
    }
  }

  private def cacheDelete(key: String): HttpResponse = {
    log.info(s"Cache DELETE request with key: $key")

    if (cache.contains(key)) {
      cache.remove(key)
      HttpResponse(
        StatusCodes.OK
      )
    } else {
      HttpResponse(
        StatusCodes.NoContent
      )
    }
  }
}
