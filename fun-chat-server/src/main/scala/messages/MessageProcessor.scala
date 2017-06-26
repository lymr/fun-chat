package messages

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import api.entities.ClientInformation
import core.entities.{User, UserID}
import messages.MessageProcessor._
import messages.entities._
import messages.parser.{MessageGenerator, TranslationError}

import scala.util.{Failure, Success, Try}

class MessageProcessor(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer)
    extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: ForwardRawMessage => processRawMessage(msg)
  }

  private def processRawMessage(rawMessage: ForwardRawMessage): Unit = {

    def dispatchMessage(message: Message): Unit = {
      val triedProcessing = Try {
        message match {
          case text: TextMessage => processTextMessage(text)
          case _: MediaMessage   => // TODO: Add support for media message
        }
      }

      triedProcessing match {
        case Success(_) => sender() ! ProcessingDone
        case Failure(ex) =>
          log.error(s"Failed processing message $rawMessage with error :=", ex)
          sender() ! ProcessingFailure("Failed to process message.")
      }
    }

    def reportTranslationError(error: TranslationError): Unit = {
      sender() ! ProcessingFailure(error.cause)
    }

    ctx.messageGenerator
      .generate(rawMessage.message.content, rawMessage.senderCtx.username, rawMessage.message.timestamp)
      .fold(dispatchMessage, reportTranslationError)
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
      //TODO: Messengers should be in a pool !!! - no need to manage errors, - attachment should have different pool (or allow only ~50% of workers to handle 'heavy' operations)
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
