package commands.runner

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import commands.ClientCommands._
import commands.entities._
import utils.{HelpPrinter, UserInformer}

import scala.io.StdIn.readLine
import scala.util.Try

class ClientCommandsLoop(executor: ActorRef, exitCallback: (Int) => Unit)
    extends StrictLogging with UserInformer{

  def start(): Unit = {
    informUserCallback(
      "Hey, welcome to fun-chat! \n" +
        "Type 'help' to show demo client commands.")

    Try {
      do {
        val input: String = readLine()
        input match {
          case cmd if cmd.startsWith(HELP)               => HelpPrinter.print()
          case cmd if cmd.startsWith(SIGN_IN)            => executor ! SignInCommand(cmd)
          case cmd if cmd.startsWith(SIGN_UP)            => executor ! SignUpCommand(cmd)
          case cmd if cmd.startsWith(SIGN_OUT)           => executor ! SignOutCommand(cmd)
          case cmd if cmd.startsWith(UPDATE_CREDENTIALS) => executor ! UpdateCredentialsCommand(cmd)
          case cmd if cmd.startsWith(ONLINE_USERS)       => executor ! ListOnlineUsersCommand(cmd)
          case cmd if cmd.startsWith(USER_INFO)          => executor ! GetUserInfoCommand(cmd)
          case cmd if cmd.startsWith(SEND_MESSAGE)       => executor ! SendMessageCommand(cmd)
          case cmd if cmd.startsWith(EXIT)               => exitCallback(0)
          case other                                     => informUserCallback(s"Unsupported command ! $other")
        }
      } while (true)
    }.recover {
      case ex: Exception =>
        informUserCallback("Unexpected error occurred!")
        logger.error("Unexpected error occurred!", ex)
    }
  }
}
