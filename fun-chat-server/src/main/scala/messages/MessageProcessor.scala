package messages

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import core.entities.{User, UserID}
import messages.MessageProcessor._
import messages.entities._
import messages.parser.{MessageGenerator, TranslationError}
import restapi.http.entities.ClientInformation

class MessageProcessor(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer)
    extends Actor
    with ActorLogging {

  override def receive: Receive = {
    case msg: ForwardRawMessage => processRawMessage(msg)
  }

  private def processRawMessage(rawMessage: ForwardRawMessage): Unit = {
    val processMessage: Message => Unit = {
      case text: TextMessage => processTextMessage(text)
      case _: MediaMessage   => // TODO: Add support for media message
    }

    val auditFailure = (failure: TranslationError) => log.error(failure.error)

    ctx.messageGenerator
      .generate(rawMessage.message.content, rawMessage.senderCtx.username, rawMessage.message.timestamp)
      .fold(processMessage, auditFailure)
  }

  private def processTextMessage(message: TextMessage): Unit = {
    val processedMessages = for {
      recipientName       <- message.recipients
      recipientUser       <- ctx.findRecipientByName(recipientName)
      recipientClientInfo <- ctx.findRecipientInfo(recipientUser.userId)
      processedMessage = ProcessedTextMessage(message.content,
                                              message.sender,
                                              recipientName,
                                              recipientClientInfo,
                                              message.timestamp)
    } yield processedMessage

    processedMessages.foreach { msg =>
      val messenger: ActorRef = context.actorOf(Messenger.props())
      messenger ! DeliverTextMessage(msg)
    }
  }
}

object MessageProcessor {

  case class MessageProcessorContext(messageGenerator: MessageGenerator,
                                     findRecipientByName: String => Option[User],
                                     findRecipientInfo: UserID => Option[ClientInformation])

  def props(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer): Props =
    Props(new MessageProcessor(ctx))
}
