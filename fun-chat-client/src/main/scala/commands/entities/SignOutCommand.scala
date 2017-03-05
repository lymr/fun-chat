package commands.entities

import commands.ClientCommands

case class SignOutCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.SIGN_OUT

  override def tokens: Set[String] = Set.empty[String]
}
