package websocket

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import api.entities.{MessageEntity, MessageProcessingResponse}
import core.entities.AuthTokenContext
import messages.MessageProcessor._
import restapi.http.JsonSupport
import spray.json._
import websocket.ClientEndpoint.{Attach, IncomingMessage, OutgoingMessage}
import websocket.ConnectedClientsStore.ClientConnected

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class ClientEndpoint(userContext: AuthTokenContext,
                     messagesRouter: ActorRef,
                     connectedClientsStore: ActorRef,
                     processingTimeout: FiniteDuration)
                    (implicit apiDispatcher: ExecutionContext)
    extends Actor with JsonSupport {

  implicit val timeout = Timeout(processingTimeout)

  override def receive: Receive = {

    case Attach(socket) =>
      connectedClientsStore ! ClientConnected(userContext.userId, self)
      context.become(online(socket))
  }

  def online(socket: ActorRef): Receive = {

    case IncomingMessage(jsContent) =>
      val messageEntity = jsContent.parseJson.convertTo[MessageEntity]
      (messagesRouter ? ForwardRawMessage(messageEntity, userContext))
        .mapTo[MessageProcessingResponse] pipeTo socket

    case DeliverRawMessage(messageEntity) =>
      val jsContent = messageEntity.toJson.toString
      socket ! OutgoingMessage(jsContent)
  }

}

object ClientEndpoint {

  def props(userContext: AuthTokenContext,
            messagesRouter: ActorRef,
            connectedClientsStore: ActorRef,
            processingTimeout: FiniteDuration)(implicit apiDispatcher: ExecutionContext): Props =
    Props(new ClientEndpoint(userContext, messagesRouter, connectedClientsStore, processingTimeout))

  def name(userAuthContext: AuthTokenContext): String = {
    s"client-endpoint-${userAuthContext.userId.id}"
  }

  case class Attach(socket: ActorRef)
  case class IncomingMessage(jsContent: String)
  case class OutgoingMessage(jsContent: String)
}
