package messages.entities

import restapi.http.entities.ClientInformation

trait Message {
  val typeName: String

  val content: String
  val sender: String
  val recipientName: String
  val recipientInfo: ClientInformation
  val timestamp: Long
}

case class TextMessage(content: String,
                       sender: String,
                       recipientName: String,
                       recipientInfo: ClientInformation,
                       timestamp: Long)
    extends Message {
  override val typeName: String = "TextMessage"
}
