package commands.entities

abstract class ClientCommand {

  /**
    * User command to execute
    */
  val command: String

  /**
    * User command input
    */
  val input: String

  /**
    * Commands expected tokens
    */
  def tokens: Set[String]

  /**
    * Commands arguments
    */
  def arguments: String = input.stripPrefix(command)
}
