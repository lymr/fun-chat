package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import restapi.http.JsonSupport

import scala.concurrent.ExecutionContext

class AuthenticationRoute(authService: AuthenticationService)(implicit ec: ExecutionContext)
    extends Directives with ContentExtractionSupport with JsonSupport {

  val route: Route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          extractCredentials {
            case Some(BasicHttpCredentials(username, password)) =>
              complete(authService.signIn(username, password))
            case _ => complete(StatusCodes.Unauthorized)
          }
        }
      }
    } ~
      path("signUp") {
        pathEndOrSingleSlash {
          post {
            extractCredentials {
              case Some(BasicHttpCredentials(username, password)) =>
                complete(authService.signUp(username, password))
              case _ => complete(StatusCodes.Unauthorized)
            }
          }
        }
      } ~
      path("signOut") {
        pathEndOrSingleSlash {
          post {
            extractUserInfo { (id, token) =>
              authorize(authService.authorize(id, token)) {
                complete(authService.signOut(id, token))
              }
            }
          }
        }
      }
  }

}
