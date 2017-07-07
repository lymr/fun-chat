package messages.entities

import core.entities.{AuthTokenContext, User}

abstract class ProcessedMessage {
  val sender: AuthTokenContext
  val recipient: User
  val timestamp: Long
}

case class ProcessedTextMessage(content: String, sender: AuthTokenContext, recipient: User, timestamp: Long) extends ProcessedMessage
