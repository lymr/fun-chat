package commands

case class SignUpCommand() extends ClientCommand {

  override val command: String = ClientCommands.SIGN_UP

  override def execute(args: Any*): Unit = {
    //TODO: continue implementation :)
  }
}
