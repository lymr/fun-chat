package messages.parser

import messages.entities.{MediaMessage, Message, TextMessage}
import messages.parser.error.RecipientsListEmptyException

import scala.concurrent.{ExecutionContext, Future}

class MessageGenerator {

  private val parser: MessageParser         = new MessageParser()
  private val translator: MessageTranslator = new MessageTranslator()

  def generate(input: String, sender: String, timestamp: Long)(implicit ec: ExecutionContext): Future[Message] = {
    parser.parse(input).map(translator.translate).map(generateMessage(_, sender, timestamp))
  }

  def generateMessage(translatedMessage: TranslatedMessage, sender: String, timestamp: Long): Message =
    translatedMessage match {
      case TranslatedMessage(_, _, r) if r.isEmpty => throw new RecipientsListEmptyException("Recipients list is empty!")
      case TranslatedMessage(c, a, r) if a.isEmpty => TextMessage(c, sender, r, timestamp)
      case TranslatedMessage(c, a, r)              => MediaMessage(c, a, sender, r, timestamp)
    }
}
