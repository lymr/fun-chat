package messages.entities

abstract class MessageProcessingResult

case object ProcessingDone extends MessageProcessingResult

case class ProcessingFailure(cause: String) extends MessageProcessingResult
