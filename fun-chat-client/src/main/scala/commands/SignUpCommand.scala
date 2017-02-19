package commands

import rest.client.RestClient

case class SignUpCommand(restClient: RestClient) extends ClientCommand {

  override val command: String = ClientCommands.SIGN_UP

  override def execute(args: Any*): Unit = {
    restClient.signUp("mor", "p@ssword")
  }
}
