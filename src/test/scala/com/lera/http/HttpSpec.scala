package com.lera.http

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalaj.http.HttpResponse


class HttpSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  override def beforeAll() = {
    val config = ConfigFactory.load("application.conf")
    val port = config.getInt("http.port")
    val host = config.getString("http.host")
    val futureBinding = akka.http.scaladsl.Http().newServerAt(host, port).bindSync(Route.requestHandler)
  }

  "The http server" should {
    "return OK for healthcheck GET request" in {
      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/healthcheck").asString
      assert(response.code.equals(200))
    }

    "return No Content for cache/key GET request if key is not found" in {
      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/kkk").asString
      assert(response.code.equals(204))
    }

    "return OK and value for cache/key GET request if key is found" in {
      val postResponse: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/key123").postForm(Seq("value" -> "value123")).asString

      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/key123").asString
      assert(response.code.equals(200))
      assert(response.body.equals("\"value\" : value123"))
    }

    "return OK for cache/key POST request if key and value are valid" in {
      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/key123").postForm(Seq("value" -> "value123")).asString
      assert(response.code.equals(200))
    }

    "return Bad Request for cache/key POST request if key is too long" in {
      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/key1dafkajdsfadjsfjdsfjasdlkfjasdlk;fjaslk;dfjalsk;dfjalksdjflaksdjfklasdjflk;asjdflk;asdjflkasdjfl;kasjdf;klajfdklajsdf23").postForm(Seq("value" -> "value123")).asString
      assert(response.code.equals(400))
    }

    "return Bad Request for cache/key POST request if value is too long" in {
      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/keyf23").postForm(Seq("value" -> "value123value123value123value123value123value123value123value123value123value123value123value123value123value123")).asString
      assert(response.code.equals(400))
    }

    "return OK for cache/key DELETE request if key is found" in {
      val postResponse: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/key123").postForm(Seq("value" -> "value123")).asString

      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/key123").method("DELETE").asString
      assert(response.code.equals(200))
    }

    "return No Content for cache/key DELETE request if key is not found" in {
      val response: HttpResponse[String] = scalaj.http.Http("http://localhost:8081/cache/keyttt").method("DELETE").asString
      assert(response.code.equals(204))
    }
  }
}
