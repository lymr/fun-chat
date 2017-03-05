package commands.executor

import akka.actor.ActorRef
import authentication.entities.{SignIn, SignOut, SignUp}
import commands.entities._
import commands.parser.CommandParser

import scala.util.{Failure, Success, Try}

class CommandExecutor(parser: CommandParser,
                      authenticationFSM: ActorRef,
                      onFailureCallback: (ClientCommand, Throwable) => Unit,
                      exitCallback: (Int) => Unit) {

  def execute(command: ClientCommand): Unit = {
    val triedParsing = Try {
      parser.parse(command.arguments, command.tokens)
    }

    triedParsing match {
      case Success(parsedArguments) => executeWithArgs(command, parsedArguments)
      case Failure(ex)              => onFailureCallback(command, ex)
    }
  }

  private def executeWithArgs(command: ClientCommand, arguments: Map[String, String]): Unit = {
    command match {
      case cmd: HelpCommand => cmd.execute()

      case cmd: SignInCommand =>
        authenticationFSM ! SignIn(arguments(SignInCommand.UserToken), arguments(SignInCommand.PasswordToken))

      case cmd: SignUpCommand =>
        authenticationFSM ! SignUp(arguments(SignInCommand.UserToken), arguments(SignInCommand.PasswordToken))

      case cmd: SignOutCommand =>
        authenticationFSM ! SignOut

      case cmd: UpdateCredentialsCommand => //TODO: add support fot update credentials
        onFailureCallback(cmd, new IllegalArgumentException("Unsupported command, yet!"))

      case cmd: ListOnlineUsersCommand =>
        onFailureCallback(cmd, new IllegalArgumentException("Unsupported command, yet!"))

      case cmd: GetUserInfoCommand =>
        onFailureCallback(cmd, new IllegalArgumentException("Unsupported command, yet!"))

      case cmd: SendMessageCommand =>
        onFailureCallback(cmd, new IllegalArgumentException("Unsupported command, yet!"))

      case cmd: ExitCommand => exitCallback(0)

      case other => onFailureCallback(other, new IllegalArgumentException("Unsupported command"))
    }
  }
}
