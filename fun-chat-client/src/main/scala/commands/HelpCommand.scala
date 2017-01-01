package commands

case class HelpCommand() extends ClientCommand {

  override val command: String = ClientCommands.HELP

  override def execute(args: Any*): Unit = {
    println(
      "signIn   -   start user authentication process into fun-chat.\n" +
        "signUp   -   register a new user to fun-chat.\n" +
        "signOut  -   sign-out authenticated user from fun-chat.\n" +
        "online users   -   print all authenticated users to system.\n" +
        "send message   -   send message to authenticated user.\n" +
        "exit     -   exit fun-chat demo client."
    )
  }
}
