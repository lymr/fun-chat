package core.entities

case class MessageEntity(content: String, sender: String, recipients: Seq[String], timestamp: Long)
