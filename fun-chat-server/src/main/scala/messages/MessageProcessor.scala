package messages

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.ActorMaterializer
import core.entities.{User, UserID}
import messages.MessageProcessor._
import messages.entities._
import restapi.http.entities.ClientInformation

class MessageProcessor(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer) extends Actor {

  override def receive: Receive = {
    case msg: ForwardRawMessage => processRawMessage(msg)
  }

  private def processRawMessage(rawMessage: ForwardRawMessage): Unit = {
    /* TODO: Parse raw message into one of:
       1. multi recipients text message
       2. multimedia message
   */
  }

  private def processTextMessage(message: TextMessage): Unit = {
    val processedMessages = for {
      recipientName       <- message.recipients
      recipientUser       <- ctx.findRecipientByName(recipientName)
      recipientClientInfo <- ctx.findRecipientInfo(recipientUser.userId)
      processedMessage = ProcessedTextMessage(message.content,
                                              message.sender.name,
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

  case class MessageProcessorContext(findRecipientByName: String => Option[User],
                                     findRecipientInfo: UserID => Option[ClientInformation])

  def props(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer): Props =
    Props(new MessageProcessor(ctx))
}
