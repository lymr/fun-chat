package commands.entities

import commands.ClientCommands
import commands.entities.GetUserInfoCommand._

case class GetUserInfoCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.USER_INFO

  override def tokens: Set[String] = Set(UserNameToken)
}

object GetUserInfoCommand {
  val UserNameToken: String = "u"
}
