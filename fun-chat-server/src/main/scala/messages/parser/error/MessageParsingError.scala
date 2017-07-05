package messages.parser.error

class MessageParsingFailure(cause: String) extends Exception(cause)

class MessageParsingError(cause: String) extends Exception(cause)
