package messages.entities

abstract class Message {
  val sender: String
  val recipients: Seq[String]
  val timestamp: Long
}

case class TextMessage(content: String, sender: String, recipients: Seq[String], timestamp: Long) extends Message

case class MediaMessage(content: String,
                        attachments: Seq[String],
                        sender: String,
                        recipients: Seq[String],
                        timestamp: Long)
    extends Message
