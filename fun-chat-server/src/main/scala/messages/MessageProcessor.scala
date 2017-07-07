package messages

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import api.entities.{MessageEntity, MessageProcessingCodes, MessageProcessingResponse}
import core.entities.AuthTokenContext
import messages.MessageProcessor._
import messages.Messenger.DeliverTextMessage
import messages.entities._
import messages.parser.MessageGenerator
import messages.parser.error.{MessageParsingError, MessageParsingFailure, RecipientsListEmptyException}

class MessageProcessor(messageGenerator: MessageGenerator, messenger: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case rawMessage: ForwardRawMessage => processRawMessage(rawMessage)
  }

  private def processRawMessage(rawMessage: ForwardRawMessage): Unit = {
    messageGenerator
      .generate(rawMessage.message.content, rawMessage.userContext.username, rawMessage.message.timestamp)
      .map {
        case text: TextMessage =>
          messenger ! DeliverTextMessage(text, rawMessage.userContext)
          MessageProcessingResponse(MessageProcessingCodes.OK)

        case _: MediaMessage => // TODO: Add support for media message
          MessageProcessingResponse(MessageProcessingCodes.NotSupported, "Media message not supported! yet.")
      }
      .recover(messageProcessingErrorHandler)
      .foreach(sender ! _)
  }

  private def messageProcessingErrorHandler: PartialFunction[Throwable, MessageProcessingResponse] = {
    case ex: MessageParsingFailure =>
      MessageProcessingResponse(MessageProcessingCodes.ParsingFailure, ex.getMessage)

    case ex: MessageParsingError =>
      MessageProcessingResponse(MessageProcessingCodes.ParsingError, ex.getMessage)

    case ex: RecipientsListEmptyException =>
      MessageProcessingResponse(MessageProcessingCodes.NoRecipientsFailure, ex.getMessage)

    case ex: Exception =>
      MessageProcessingResponse(MessageProcessingCodes.GenerationError, ex.getMessage)
  }
}

object MessageProcessor {

  def props(messageGenerator: MessageGenerator, messenger: ActorRef): Props =
    Props(new MessageProcessor(messageGenerator, messenger))

  case class DeliverRawMessage(message: MessageEntity)
  case class ForwardRawMessage(message: MessageEntity, userContext: AuthTokenContext)
}
