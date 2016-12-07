package restapi.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.authentication.AuthenticationService
import core.db.DatabaseContext

import scala.concurrent.ExecutionContext

class HttpRouter(dbc: DatabaseContext, authService: AuthenticationService)(implicit ec: ExecutionContext) {

  private val userRoute = new UsersRoute(dbc.usersDao)
  private val authRoute = new AuthenticationRoute(authService)

  val routes: Route = pathPrefix("v1") {
    authRoute.route ~
      userRoute.route
  }
}
