package messages.parser

import com.typesafe.scalalogging.StrictLogging
import messages.parser.MessageConstants._
import messages.parser.error._
import utils.StringUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.parsing.combinator.{JavaTokenParsers, PackratParsers}

object MessageConstants {
  val SendOperator       = "send"
  val MessageOperator    = "message"
  val AttachmentOperator = "attachment"
  val SubjectOperator    = "to"
  val CommaOperator      = ","
  val SemicolonOperator  = ";"
}

class MessageParser extends JavaTokenParsers with PackratParsers with StrictLogging {

  private val delimiterOperator: PackratParser[String] = literal(CommaOperator) | literal(SemicolonOperator)
  private val subjectOperator: PackratParser[String]   = literal(SubjectOperator)
  private val contentOperator: PackratParser[String]   = literal(MessageOperator) | literal(AttachmentOperator)
  private val sendOperator: PackratParser[String]      = literal(SendOperator)

  private val entity: PackratParser[Entity] = stringLiteral ^^ { e => Entity(e.stripMargins("\"")) }

  private val contentEntity: PackratParser[Content] = (contentOperator ~ entity) ^^ {
    case op ~ en => Content(en, op)
  }

  private val contentEntities: PackratParser[Seq[Content]] = repsep(contentEntity, delimiterOperator)

  private val subjectEntities: PackratParser[Seq[Entity]] = repsep(entity, delimiterOperator)

  private val toEntity: PackratParser[To] = (subjectOperator ~> subjectEntities) ^^ { subjects => To(subjects) }

  private val sendOperation: PackratParser[Operation] = (sendOperator ~> toEntity ~ contentEntities) ^^ {
    case subjects ~ content => Send(content, subjects)
  }

  def parse(text: String)(implicit ec: ExecutionContext): Future[Operation] = Future {
    val parsedResult = parseAll(sendOperation, text)
    parsedResult match {
      case Success(r, _)  => r

      case Failure(cause, _) =>
        logger.error("ParsingError: Failed parsing message.", cause)
        throw new MessageParsingFailure(s"Failed parsing message, Cause:= $cause")

      case Error(cause, _)   =>
        logger.error("ParsingError: An Error occurred while parsing message", cause)
        throw new MessageParsingError(s"An Error occurred while parsing message, Cause:= $cause")
    }
  }
}