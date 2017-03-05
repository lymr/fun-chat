package commands.entities

import commands.ClientCommands

case class ListOnlineUsersCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.ONLINE_USERS

  override def tokens: Set[String] = Set.empty[String]
}
