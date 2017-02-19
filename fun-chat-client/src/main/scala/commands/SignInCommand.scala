package commands

import rest.client.RestClient

case class SignInCommand(restClient: RestClient) extends ClientCommand {
  override val command: String = ClientCommands.SIGN_IN

  override def execute(args: Any*): Unit = {
    restClient.signIn("mor", "p@ssword")
  }
}
