package restapi.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import core.db.DatabaseContext
import core.entities.Defines.AuthToken
import core.entities.User
import restapi.http.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

class HttpRouter(dbc: DatabaseContext, authService: AuthenticationService)(implicit ec: ExecutionContext)
    extends Directives with JsonSupport {

  implicit val ac: AuthorizationContext = new AuthorizationContext(authService.authorize, dbc.usersDao.findUserByName)

  private val userRoute = new UsersRoute(dbc.usersDao)
  private val authRoute = new AuthenticationRoute(authService)

  val routes: Route = pathPrefix("v1") {
    authRoute.route ~
      userRoute.route
  }
}

class AuthorizationContext(val tokenAuthorizer: (AuthToken) => Future[Option[User]],
                           val findUserByName: (String) => Option[User])
