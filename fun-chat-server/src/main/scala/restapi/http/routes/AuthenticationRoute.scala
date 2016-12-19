package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import restapi.http.JsonSupport
import restapi.http.entities.UserCredentialsEntity

import scala.concurrent.ExecutionContext

class AuthenticationRoute(authService: AuthenticationService)(implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

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
          securedAccess { ctx =>
            post {
              complete(authService.signOut(ctx.userId))
            }
          }
        }
      }
  } ~
      pathPrefix("credentials") {
      pathEndOrSingleSlash {
        securedAccess { ctx =>
          entity(as[UserCredentialsEntity]) { credentials =>
            patch {
              complete(authService.updateCredentials(ctx.userId, credentials.username, credentials.password))
            }
          }
        }
      }
    }

}
