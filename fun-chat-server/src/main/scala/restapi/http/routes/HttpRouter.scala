package restapi.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import core.db.DatabaseContext
import core.db.clients.ConnectedClientsStore
import core.entities.Defines.AuthToken
import core.entities.{TokenContext, User}
import restapi.http.JsonSupport
import restapi.http.routes.support.CorsSupport

import scala.concurrent.{ExecutionContext, Future}

class HttpRouter(dbc: DatabaseContext, authService: AuthenticationService, connectedClients: ConnectedClientsStore)(
    implicit ec: ExecutionContext) extends Directives with JsonSupport with CorsSupport {

  private implicit val ac = new ApiContext(authService.authorize, dbc.usersDao.findUserByName)

  private val userRoute      = new UsersRoute(dbc.usersDao)
  private val authRoute      = new AuthenticationRoute(authService)
  private val messagingRoute = new MessagingRoute()

  val routes: Route = pathPrefix("v1") {
    AccessControlCheck {
      authRoute.route ~
        userRoute.route ~
        messagingRoute.route
    }
  }
}

class ApiContext(val authenticate: (AuthToken) => Future[Option[TokenContext]],
                 val findUserByName: (String) => Option[User])
