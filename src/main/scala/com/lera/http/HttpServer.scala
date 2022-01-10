package com.lera.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory

/**
 * Starts a HTTP server and binds routs.
 *
 */
object HttpServer extends App {
  implicit val system: ActorSystem = ActorSystem("http-server")
  implicit val log = Logging(system, "main")

  private val config = ConfigFactory.load("application.conf")
  private val port = config.getInt("http.port")
  private val host = config.getString("http.host")

  val futureBinding = Http().newServerAt(host, port).bindSync(Route.requestHandler)

  log.info(s"Server started at the port $port")
}