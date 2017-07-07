package websocket

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import core.entities.AuthTokenContext
import websocket.ClientEndpoint._
import websocket.WebSocketHandler._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class WebSocketHandler(processingRouter: ActorRef, connectedClientsStore: ActorRef, processingTimeout: FiniteDuration)
                      (implicit system: ActorSystem, mat: ActorMaterializer, apiDispatcher: ExecutionContext) {

  def clientEndpoint(ctx: AuthTokenContext): Flow[Message, Message, NotUsed] = {
    val clientEndpoint =
      system.actorOf(ClientEndpoint.props(ctx, processingRouter, connectedClientsStore, processingTimeout),
                     ClientEndpoint.name(ctx))

    val incomingMessages: Sink[Message, NotUsed] = Flow[Message]
      .map {
        case TextMessage.Strict(jsContent) => Some(IncomingMessage(jsContent))

        case ts: TextMessage.Streamed =>
          ts.textStream.runWith(Sink.ignore)
          None

        case br: BinaryMessage =>
          br.dataStream.runWith(Sink.ignore)
          None
      }
      .collect {
        case Some(message: IncomingMessage) => message
      }
      .to(Sink.actorRef[IncomingMessage](clientEndpoint, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] = Source
      .actorRef[OutgoingMessage](BUFFER_SIZE, OverflowStrategy.backpressure)
      .mapMaterializedValue { socket =>
        clientEndpoint ! Attach(socket)
        NotUsed
      }
      .map {
        case OutgoingMessage(jsContent) => TextMessage(jsContent)
      }

    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }
}

object WebSocketHandler {
  private val BUFFER_SIZE: Int = 10
}
