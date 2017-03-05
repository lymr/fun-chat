package commands.entities

import commands.ClientCommands

case class SendMessageCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.SEND_MESSAGE

  override def tokens: Set[String] = Set.empty[String]
}
