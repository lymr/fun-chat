package restapi.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import core.db.DatabaseContext
import core.db.users.ConnectedClientsStore
import core.entities.Defines.{AuthToken, UserID}
import core.entities.{ClientInformation, TokenContext, User}
import restapi.http.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

class HttpRouter(dbc: DatabaseContext, authService: AuthenticationService, connectedClients: ConnectedClientsStore)(
    implicit ec: ExecutionContext)
    extends Directives
    with JsonSupport {

  private implicit val ac = new ApiContext(authService.authorize, dbc.usersDao.findUserByName, connectedClients.update)

  private val userRoute      = new UsersRoute(dbc.usersDao)
  private val authRoute      = new AuthenticationRoute(authService)
  private val messagingRoute = new MessagingRoute()

  val routes: Route = pathPrefix("v1") {
    authRoute.route ~
      userRoute.route ~
      messagingRoute.route
  }
}

class ApiContext(val authenticate: (AuthToken) => Future[Option[TokenContext]],
                 val findUserByName: (String) => Option[User],
                 val updateClientAddress: (UserID, ClientInformation) => Unit)
