package commands.entities

import commands.ClientCommands

case class ExitCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.EXIT

  override def tokens: Set[String] = Set.empty[String]
}
