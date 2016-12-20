package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import restapi.http.JsonSupport
import restapi.http.routes.support.SecuredAccessSupport

import scala.concurrent.ExecutionContext

class MessagingRoute(implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

  val route: Route = pathPrefix("messages") {
    securedAccess { ctx =>
      pathEndOrSingleSlash {
        post {
          // broadcast message to all sers
          complete(StatusCodes.OK)
        }
      } ~
        path("username" / Segment) { username =>
          pathEndOrSingleSlash {
            post {
              complete(StatusCodes.OK, username)
            }
          }
        }
    }
  }

}
