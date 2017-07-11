import akka.actor.ActorSystem
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import core.authentication._
import core.authentication.tokenGenerators._
import core.db.{DatabaseContext, FlywayService}
import core.entities.Timer
import messages.parser.MessageGenerator
import messages.{MessageProcessor, Messenger}
import restapi.http.HttpService
import restapi.http.routes.HttpRouter
import utils.Configuration
import websocket.{ConnectedClientsStore, WebSocketHandler}

import scala.concurrent.ExecutionContext

class Bootstrap {

  def startup(): Unit = {
    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config        = new Configuration()
    val dbc           = new DatabaseContext()
    val flywayService = new FlywayService(config)
    flywayService.migrateDatabaseSchema()

    val bearerTokenGenerator = new JwtBearerTokenGenerator(SecuredTokenGenerator.generate, Timer(config.tokenExpiration))
    val userAuthenticator    = new UserAuthenticator(UserSecretUtils.validate, bearerTokenGenerator, dbc.credentialsDao)
    val connectedClients     = actorSystem.actorOf(ConnectedClientsStore.props(), "connected-clients-store")

    val messageGenerator = new MessageGenerator()
    val messagesRouter   = actorSystem.actorOf(FromConfig.props(Messenger.props(dbc.usersDao)), "messagesRouter")
    val processingRouter = actorSystem.actorOf(FromConfig.props(MessageProcessor.props(messageGenerator, messagesRouter)), "processingRouter")

    val apiDispatcher: ExecutionContext = actorSystem.dispatchers.lookup("akka.blocking-api-dispatcher")
    val webSocketHandler = new WebSocketHandler(connectedClients, processingRouter, config.messageTimeout)(actorSystem, materializer, apiDispatcher)
    val authService = new AuthenticationService(userAuthenticator, dbc.usersDao, connectedClients)
    val httpRouter  = new HttpRouter(dbc, authService, webSocketHandler, processingRouter, config)(apiDispatcher)
    val httpService = new HttpService(httpRouter, config)
    httpService.start()
  }
}
