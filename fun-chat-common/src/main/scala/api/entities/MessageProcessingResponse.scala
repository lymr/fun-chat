package api.entities

import api.entities.MessageProcessingCodes.MessageProcessingCode

/**
  * All possible results for a message request
  */
object MessageProcessingCodes {

  /**
    * A result code for message processing
    * @param code Result code
    * @param details Result code details
    */
  case class MessageProcessingCode(code: Int, details: String)

  val OK                  = MessageProcessingCode(1000, "Completed")
  val ParsingFailure      = MessageProcessingCode(1200, "Failed to parse message")
  val ParsingError        = MessageProcessingCode(1201, "Unexpected error occurred while parsing message")
  val TranslationError    = MessageProcessingCode(1300, "Failed to translate message")
  val GenerationError     = MessageProcessingCode(1400, "Failed to generate message")
  val NoRecipientsFailure = MessageProcessingCode(1401, "Recipients list is empty!")
  val NotSupported        = MessageProcessingCode(3000, "Method / Message Type not supported.")
  val Error               = MessageProcessingCode(5000, "Unexpected error!")
}

/**
  * A response entity for message request
  * @param code The message processing result code
  * @param message An additional information regarding the result
  */
case class MessageProcessingResponse(code: MessageProcessingCode, message: Option[String])

object MessageProcessingResponse {

  def apply(code: MessageProcessingCode): MessageProcessingResponse =
    MessageProcessingResponse(code, None)

  def apply(code: MessageProcessingCode, message: String): MessageProcessingResponse =
    MessageProcessingResponse(code, Some(message))
}
