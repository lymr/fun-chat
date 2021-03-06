package restapi.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.{Directives, Route}
import core.authentication.AuthenticationService
import core.db.DatabaseContext
import core.entities.{AuthToken, AuthTokenContext, User}
import restapi.http.JsonSupport
import restapi.http.routes.support.CorsSupport
import utils.Configuration
import websocket.WebSocketHandler

import scala.concurrent.ExecutionContext

class HttpRouter(dbc: DatabaseContext,
                 authService: AuthenticationService,
                 webSocketHandler: WebSocketHandler,
                 processingRouter: ActorRef,
                 configuration: Configuration)(implicit apiDispatcher: ExecutionContext)
    extends Directives with JsonSupport with CorsSupport {

  private implicit val ac = new ApiContext(authService.authorize, dbc.usersDao.findUserByName)

  private val userRoute = new UsersRoute(dbc.usersDao)
  private val authRoute = new AuthenticationRoute(authService)
  private val messagesRoute = new MessagesRoute(processingRouter, webSocketHandler, configuration.messageTimeout)

  val routes: Route = pathPrefix("v1") {
    AccessControlCheck {
      authRoute.route ~
        userRoute.route ~
        messagesRoute.route
    }
  }
}

class ApiContext(val authenticate: AuthToken => Option[AuthTokenContext],
                 val findUserByName: String => Option[User])
