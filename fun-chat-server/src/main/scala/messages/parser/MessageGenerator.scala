package messages.parser

import messages.entities.{MediaMessage, Message, TextMessage}

class MessageGenerator {

  private val parser: MessageParser         = new MessageParser()
  private val translator: MessageTranslator = new MessageTranslator()

  def generate(input: String, sender: String, timestamp: Long): Either[Message, TranslationError] = {
    val maybeParsedMessage = parser.parse(input).flatMap(translator.translate)
    maybeParsedMessage match {
      case Some(ParsedMessage(_, _, r)) if r.isEmpty => Right(TranslationError("Recipients list is empty!"))
      case Some(ParsedMessage(c, a, r)) if a.isEmpty => Left(TextMessage(c, sender, r, timestamp))
      case Some(ParsedMessage(c, a, r))              => Left(MediaMessage(c, a, sender, r, timestamp))
      case None                                      => Right(TranslationError("Message translation failed!"))
    }
  }
}

case class TranslationError(cause: String)
