package messages.entities

import restapi.http.entities.ClientInformation

abstract class ProcessedMessage {
  val senderName: String
  val recipientName: String
  val recipientClientInfo: ClientInformation
  val timestamp: Long
}

case class ProcessedTextMessage(content: String,
                                senderName: String,
                                recipientName: String,
                                recipientClientInfo: ClientInformation,
                                timestamp: Long)
