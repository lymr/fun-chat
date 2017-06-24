package commands.parser

import commands.parser.CommandParser._

class CommandParser {

  def parse(args: String, commandTokens: Set[String]): Map[String, String] = {
    if (commandTokens.isEmpty) Map.empty[String, String] else parseArgumentsString(args, commandTokens)
  }

  private def parseArgumentsString(args: String, commandTokens: Set[String]): Map[String, String] = {

    def toKeyValuePair(z: Array[String]): (String, String) = {
      val filtered = z.filterNot(x => x.isEmpty || x.equals(TokensDelimiter))

      require(filtered.nonEmpty, "Key-Value pair is empty!")
      require(commandTokens.contains(filtered.head), "Unrecognized token received")
      require(filtered.length == 2, "Missing or to many argument(s).")

      val tokenKey   = filtered.head
      val tokenValue = filtered.tail.mkString.replace("\\s+", "")
      tokenKey -> tokenValue
    }

    args
      .dropWhile(x => x.isWhitespace)
      .split(ArgumentsDelimiter)
      .filter(_.nonEmpty)
      .map(_.split(TokensDelimiter))
      .map(toKeyValuePair)
      .toMap
  }
}

object CommandParser {
  private val ArgumentsDelimiter: String = "-"
  private val TokensDelimiter: String    = " "
}
