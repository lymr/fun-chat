package restapi.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import messages.entities.{ForwardRawMessage, MessageProcessingResult, ProcessingDone, ProcessingFailure}
import restapi.http.JsonSupport
import restapi.http.entities.MessageEntity
import restapi.http.routes.support.SecuredAccessSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MessagesRoute(messagesRouter: ActorRef, processingTimeout: FiniteDuration)
                   (implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

  implicit val timeout = Timeout(processingTimeout)

  val route: Route = pathPrefix("messages") {
    securedAccess { ctx =>
      pathEndOrSingleSlash {
        post {
          decodeRequest {
            entity(as[MessageEntity]) { message =>
              val messageFuture = { messagesRouter ? ForwardRawMessage(message, ctx) }.mapTo[MessageProcessingResult]
              onSuccess(messageFuture) {
                case ProcessingDone           => complete(StatusCodes.OK)
                case ProcessingFailure(cause) => complete(StatusCodes.BadRequest, cause)
              }
            }
          }
        }
      }
    }
  }

}
