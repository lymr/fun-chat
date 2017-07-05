package messages

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import api.entities.{MessageEntity, MessageProcessingCodes, MessageProcessingResponse}
import core.db.users.UsersDao
import core.entities.User
import messages.MessageProcessor._
import messages.entities._
import messages.parser.MessageGenerator
import messages.parser.error.{MessageParsingError, MessageParsingFailure, RecipientsListEmptyException}

import scala.concurrent.ExecutionContext

class MessageProcessor(messageGenerator: MessageGenerator, usersDao: UsersDao)(implicit ec: ExecutionContext)
    extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: ForwardRawMessage => processRawMessage(msg, sender)
  }

  private def processRawMessage(rawMessage: ForwardRawMessage, replyTo: ActorRef): Unit = {
    messageGenerator
      .generate(rawMessage.message.content, rawMessage.sender.name, rawMessage.message.timestamp)
      .map {
        case text: TextMessage =>
          processTextMessage(text, rawMessage.sender)
          MessageProcessingResponse(MessageProcessingCodes.OK)

        case _: MediaMessage => // TODO: Add support for media message
          MessageProcessingResponse(MessageProcessingCodes.NotSupported, "Media message not supported! yet.")
      }
      .recover(messageProcessingErrorHandler)
      .foreach(replyTo ! _)
  }

  private def processTextMessage(message: TextMessage, user: User): Unit = {
    val processedMessages = for {
      recipientName <- message.recipients
      recipientUser <- usersDao.findUserByName(recipientName)
      processedMessage = ProcessedTextMessage(message.content, user, recipientUser, message.timestamp)
    } yield processedMessage

    processedMessages.foreach { msg =>
      //TODO: messenger ! DeliverTextMessage(msg)
    }
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

  def props(messageGenerator: MessageGenerator, usersDao: UsersDao)(
      implicit processingDispatcher: ExecutionContext): Props =
    Props(new MessageProcessor(messageGenerator, usersDao))

  case class DeliverRawMessage(message: MessageEntity)
  case class ForwardRawMessage(message: MessageEntity, sender: User)
}
