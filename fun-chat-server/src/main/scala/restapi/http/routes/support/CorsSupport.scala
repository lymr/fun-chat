package restapi.http.routes.support

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import restapi.http.routes.support.AllowedOrigins._

import scala.collection.immutable.Seq

private[http] trait CorsSupport {

  def AccessControlCheck(inner: Route): Route = {
    checkSameOrigin(HttpOriginRange.Default(allowedOrigins)) {
      preflightCorsCheck ~ inner
    }
  }

  private def preflightCorsCheck: Route = {
    options {
      complete {
        HttpResponse(StatusCodes.OK).withHeaders(
          `Access-Control-Allow-Origin`(HttpOriginRange.Default(allowedOrigins)),
          `Access-Control-Allow-Credentials`(allow = true),
          `Access-Control-Allow-Methods`(GET, POST, PATCH, DELETE),
          `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With"))
      }
    }
  }

}

object AllowedOrigins {
  private val HTTP_ORIGIN  = HttpOrigin("http", Host("fun-chat", 8080))
  private val HTTPS_ORIGIN = HttpOrigin("https", Host("fun-chat", 443))

  val allowedOrigins = Seq(HTTP_ORIGIN, HTTPS_ORIGIN)
}
