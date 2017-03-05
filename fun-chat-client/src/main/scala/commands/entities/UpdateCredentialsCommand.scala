package commands.entities

import commands.ClientCommands
import commands.entities.UpdateCredentialsCommand._

case class UpdateCredentialsCommand(input: String) extends ClientCommand {

  override val command: String = ClientCommands.UPDATE_CREDENTIALS

  override def tokens: Set[String] = Set(PasswordToken)
}

object UpdateCredentialsCommand {
  val PasswordToken: String = "p"
}
