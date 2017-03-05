package commands.entities

import commands.ClientCommands
import commands.entities.SignInCommand._

case class SignInCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.SIGN_IN

  override def tokens: Set[String] = Set(UserToken, PasswordToken)
}

object SignInCommand {
  val UserToken: String     = "u"
  val PasswordToken: String = "p"
}
