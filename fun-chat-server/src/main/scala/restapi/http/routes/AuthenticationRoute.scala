package restapi.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import restapi.http.JsonSupport
import restapi.http.routes.entities.{AuthEntity, CredentialsEntity}

import scala.concurrent.ExecutionContext

class AuthenticationRoute(authService: AuthenticationService)(implicit ec: ExecutionContext) extends Directives with JsonSupport {

  val route: Route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[CredentialsEntity]) { credentials =>
            complete {
              authService.signIn(credentials.login, credentials.password)
            }
          }
        }
      }
    } ~
      path("signUp") {
        pathEndOrSingleSlash {
          post {
            entity(as[CredentialsEntity]) { credentials =>
              complete {
                authService.signUp(credentials.login, credentials.password)
              }
            }
          }
        }
      } ~
      path("signOut") {
        pathEndOrSingleSlash {
          post {
            entity(as[AuthEntity]) { active =>
              complete {
                authService.signOut(active.login, active.token)
              }
            }
          }
        }
      }
  }

}
