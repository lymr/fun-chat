package messages.entities

import core.entities.User

abstract class ProcessedMessage {
  val sender: User
  val recipient: User
  val timestamp: Long
}

case class ProcessedTextMessage(content: String, sender: User, recipient: User, timestamp: Long) extends ProcessedMessage
