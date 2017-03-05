package commands.entities

import commands.ClientCommands
import commands.entities.SignUpCommand._

case class SignUpCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.SIGN_UP

  override def tokens: Set[String] = Set(UserToken, PasswordToken)
}

object SignUpCommand {
  val UserToken: String     = "u"
  val PasswordToken: String = "p"
}
