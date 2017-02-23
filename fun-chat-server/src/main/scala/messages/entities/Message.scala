package messages.entities

import core.entities.User

abstract class Message {
  val sender: User
  val recipients: Seq[String]
  val timestamp: Long
}

case class TextMessage(content: String, sender: User, recipients: Seq[String], timestamp: Long) extends Message
