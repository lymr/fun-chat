package restapi.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import messages.entities.ForwardRawMessage
import restapi.http.JsonSupport
import restapi.http.entities.MessageEntity
import restapi.http.routes.support.SecuredAccessSupport

import scala.concurrent.ExecutionContext

class MessagesRoute(messagesRouter: ActorRef)(implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

  val route: Route = pathPrefix("messages") {
    securedAccess { ctx =>
      pathEndOrSingleSlash {
        post {
          decodeRequest {
            entity(as[MessageEntity]) { message =>
              messagesRouter ! ForwardRawMessage(message, ctx)
              complete(StatusCodes.Accepted)
            }
          }
        }
      }
    }
  }

}
