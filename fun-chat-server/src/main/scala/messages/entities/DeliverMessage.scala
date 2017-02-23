package messages.entities

abstract class DeliverMessage

case class DeliverTextMessage(message: ProcessedTextMessage) extends DeliverMessage
