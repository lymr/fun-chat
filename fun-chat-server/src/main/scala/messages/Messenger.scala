package messages

import akka.actor.{Actor, ActorLogging, Props}
import core.db.users.UsersDao
import core.entities.AuthTokenContext
import messages.Messenger.DeliverTextMessage
import messages.entities.{ProcessedTextMessage, TextMessage}

class Messenger(usersDao: UsersDao) extends Actor with ActorLogging {

  override def receive: Receive = {
    case DeliverTextMessage(textMessage, userContext) => deliverTextMessage(textMessage, userContext)
  }

  private def deliverTextMessage(message: TextMessage, userContext: AuthTokenContext): Unit = {
    val processedMessages = for {
      recipientName <- message.recipients
      recipientUser <- usersDao.findUserByName(recipientName)
      processedMessage = ProcessedTextMessage(message.content, userContext, recipientUser, message.timestamp)
    } yield processedMessage

    processedMessages.foreach { msg =>
      //TODO: messenger ! DeliverTextMessage(msg)
    }
  }
}

object Messenger {
  def props(usersDao: UsersDao): Props = Props(new Messenger(usersDao))

  case class DeliverTextMessage(text: TextMessage, userContext: AuthTokenContext)

}
