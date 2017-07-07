package restapi.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import api.entities.{MessageEntity, MessageProcessingCodes, MessageProcessingResponse}
import core.entities.AuthTokenContext
import messages.MessageProcessor.ForwardRawMessage
import restapi.http.JsonSupport
import restapi.http.routes.support.SecuredAccessSupport
import websocket.WebSocketHandler

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class MessagesRoute(processingRouter: ActorRef, webSocketHandler: WebSocketHandler, processingTimeout: FiniteDuration)
                   (implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

  implicit val timeout = Timeout(processingTimeout)

  val route: Route =
    pathPrefix("messages") {
      securedAccess { ctx =>
        pathEndOrSingleSlash {
          post {
            decodeRequest {
              entity(as[MessageEntity]) { message =>
                complete(handleMessage(message, ctx))
              }
            }
          }
        } ~ path("ws") {
          get {
            handleWebSocketMessages(webSocketHandler.clientEndpoint(ctx))
          }
        }
      }
    }

  private def handleMessage(message: MessageEntity, ctx: AuthTokenContext): Future[MessageProcessingResponse] = {
    (processingRouter ? ForwardRawMessage(message, ctx))
      .mapTo[MessageProcessingResponse]
      .recover {
        case ex: Exception => MessageProcessingResponse(MessageProcessingCodes.Error, ex.getMessage)
      }
  }

}
