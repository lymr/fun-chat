package commands.executor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import authentication.entities._
import commands.entities._
import commands.executor.CommandExecutor._
import commands.parser.CommandParser
import messages.entities._
import org.joda.time.DateTime
import rest.client.RestClient
import utils.UserInformer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class CommandExecutor(parser: CommandParser, authenticationFSM: ActorRef, restClient: RestClient, messenger: ActorRef)
    extends Actor with ActorLogging with UserInformer {

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = offline

  def offline: Receive = {
    case cmd: SignInCommand =>
      executeWithArguments(cmd) { args =>
        authenticationFSM ! SignIn(args(SignInCommand.UserToken), args(SignInCommand.PasswordToken))
      }

    case cmd: SignUpCommand =>
      executeWithArguments(cmd) { args =>
        authenticationFSM ! SignUp(args(SignInCommand.UserToken), args(SignInCommand.PasswordToken))
      }

    case other: ClientCommand =>
      informUserCallback(s"Command ${other.command} is not supported while Offline.")

    case Authenticated => context.become(online)

    case AuthFailure(err) =>
      informUserCallback("Authentication failed.")
      log.error("Failed to authenticate, cause:= ", err)
  }

  def online: Receive = {
    case SignOutCommand => authenticationFSM ! SignOut

    case cmd: UpdateCredentialsCommand =>
      executeWithArguments(cmd) { args =>
        authenticationFSM ! UpdateCredentials(args(UpdateCredentialsCommand.PasswordToken))
      }

    case ListOnlineUsersCommand =>
      restClient.listOnlineUsers()
        .map { users =>
          users.foreach { user =>
            informUserCallback(
              s"User:= '${user.name}', LastSeen:= '${new DateTime(user.lastSeen).formatted(DATE_FORMAT)}'.")
          }
        }
        .onFailure {
          case error: Throwable =>
            log.error("Failed to list all online users, Cause:=", error)
            informUserCallback("Failed to retrieve online users")
        }

    case cmd: GetUserInfoCommand =>
      executeWithArguments(cmd) { args =>
        val userName = args(GetUserInfoCommand.UserNameToken)
        restClient.findUserInformation(userName)
          .map { user =>
            informUserCallback(
              s"User:= '${user.name}', LastSeen:= '${new DateTime(user.lastSeen).formatted(DATE_FORMAT)}'.")
          }
          .onFailure {
            case error: Throwable =>
              log.error(s"Failed to find user '$userName' information, Cause:=", error)
              informUserCallback(s"Failed to retrieve user '$userName' information.")
          }
      }

    case cmd: SendMessageCommand => messenger ! DeliverMessage(cmd.input)

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
  def props(parser: CommandParser, authenticationFSM: ActorRef, restClient: RestClient, messenger: ActorRef): Props =
    Props(new CommandExecutor(parser, authenticationFSM, restClient, messenger))

  private val DATE_FORMAT: String = "dd/MM/YYYY HH:mm:ss"

}
