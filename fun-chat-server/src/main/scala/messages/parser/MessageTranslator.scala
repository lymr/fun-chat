package messages.parser

import messages.parser.MessageConstants._

class MessageTranslator {

  def translate(operation: Operation): Option[ParsedMessage] = operation match {
    case Send(content, recipients) => evaluateSend(content, recipients)
    case _                         => None
  }

  private def evaluateSend(content: Seq[Content], to: To): Option[ParsedMessage] = {
    val recipients             = evaluateRecipients(to)
    val (message, attachments) = evaluateContent(content)

    Some(ParsedMessage(message, attachments, recipients))
  }

  private def evaluateRecipients(to: To): Seq[String] = to.recipients.map(_.value)

  private def evaluateContent(content: Seq[Content]): (String, Seq[String]) = {
    val message = content
      .collect {
        case Content(entity, MessageOperator) => entity.value
      }
      .mkString("\n\n")

    val attachments = content.collect {
      case Content(entity, AttachmentOperator) => entity.value
    }

    (message, attachments)
  }
}

case class ParsedMessage(content: String, attachments: Seq[String], recipients: Seq[String])
