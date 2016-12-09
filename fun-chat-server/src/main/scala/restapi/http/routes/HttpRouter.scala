package restapi.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import core.db.DatabaseContext
import restapi.http.JsonSupport

import scala.concurrent.ExecutionContext

class HttpRouter(dbc: DatabaseContext, authService: AuthenticationService)(implicit ec: ExecutionContext)
    extends Directives with ContentExtractionSupport with JsonSupport {

  private val userRoute = new UsersRoute(dbc.usersDao)
  private val authRoute = new AuthenticationRoute(authService)

  val routes: Route = pathPrefix("v1") {
    authRoute.route ~
      extractUserInfo { (id, token) =>
        authorize(authService.authorize(id, token)) {
          userRoute.route
        }
      }
  }
}
