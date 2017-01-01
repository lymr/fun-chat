package messages

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.ActorMaterializer
import core.entities.Defines.UserID
import core.entities.User
import messages.MessageProcessor._
import messages.entities._
import restapi.http.entities.ClientInformation

class MessageProcessor(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer) extends Actor {

  override def receive: Receive = {
    case msg: ForwardRawMessage  => processRawMessage(msg)
    case msg: ForwardUserMessage => processUserMessage(msg)
  }

  private def processRawMessage(rawMsg: ForwardRawMessage): Unit = {
    /* TODO: Parse raw message into one of:
       1. broadcast message
       2. multi recipients message
   */
  }

  def processUserMessage(userMsg: ForwardUserMessage): Unit = {
    val maybeMessage = for {
      recipientUser   <- ctx.findRecipientByName(userMsg.recipientName)
      recipientUserId <- recipientUser.userId
      recipientInfo   <- ctx.findRecipientInfo(recipientUserId)
      message <- Some(
        TextMessage(userMsg.message.content,
                    userMsg.senderCtx.username,
                    userMsg.recipientName,
                    recipientInfo,
                    userMsg.message.timestamp))
    } yield message

    maybeMessage.foreach { msg =>
      val messenger: ActorRef = context.actorOf(Messenger.props())
      messenger ! DeliverMessage(msg)
    }
  }
}

object MessageProcessor {

  case class MessageProcessorContext(findRecipientByName: (String) => Option[User],
                                     findRecipientInfo: (UserID) => Option[ClientInformation])

  def props(ctx: MessageProcessorContext)(implicit materializer: ActorMaterializer): Props =
    Props(new MessageProcessor(ctx))
}
