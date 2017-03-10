package commands.executor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import authentication.entities._
import commands.entities._
import commands.parser.CommandParser
import messages.entities._
import utils.UserInformer

import scala.util.{Failure, Success, Try}

class CommandExecutor(parser: CommandParser, authenticationFSM: ActorRef, messanger: ActorRef)
    extends Actor with ActorLogging with UserInformer {

  override def receive: Receive = offline

  def offline: Receive = {
    case cmd: SignInCommand => executeWithArguments(cmd) { args =>
      authenticationFSM ! SignIn(args(SignInCommand.UserToken), args(SignInCommand.PasswordToken))
    }

    case cmd: SignUpCommand => executeWithArguments(cmd) { args =>
      authenticationFSM ! SignUp(args(SignInCommand.UserToken), args(SignInCommand.PasswordToken))
    }

    case other: ClientCommand => informUserCallback(s"Command ${other.command} is not supported while Offline.")

    case Authenticated => context.become(online)

    case AuthFailure(err) =>
      informUserCallback("Authentication failed.")
      log.error("Failed to authenticate, cause:= ", err)
  }

  def online: Receive = {
    case SignOutCommand => authenticationFSM ! SignOut

    case cmd: UpdateCredentialsCommand => executeWithArguments(cmd) { args =>
      authenticationFSM ! UpdateCredentials(args(UpdateCredentialsCommand.PasswordToken))
    }

    case ListOnlineUsersCommand => messanger ! ListOnlineUsers

    case cmd: GetUserInfoCommand => executeWithArguments(cmd) { args =>
      messanger ! GetUserInformation(args(GetUserInfoCommand.UserNameToken))
    }

    case cmd: SendMessageCommand => messanger ! DeliverMessage(cmd.input)

    case other: ClientCommand => informUserCallback(s"Command ${other.command} is not supported while Online.")

    case Disconnected => context.become(offline)

    case AuthFailure(err) =>
      informUserCallback("Disconnection failed.")
      log.error("Failed to disconnect, cause:= ", err)
  }

  private def executeWithArguments(command: ClientCommand)(execute: Map[String, String] => Unit): Unit = {
    val triedParsing = Try {
      parser.parse(command.arguments, command.tokens)
    }

    triedParsing match {
      case Success(parsedArguments) => execute(parsedArguments)
      case Failure(ex) =>
        informUserCallback("Execution failed.")
        log.error(s"Failed parsing command $command with error:= ", ex)
    }
  }
}

object CommandExecutor {
  def props(parser: CommandParser, authenticationFSM: ActorRef, messanger: ActorRef): Props =
    Props(new CommandExecutor(parser, authenticationFSM, messanger))
}
