package commands.runner

import com.typesafe.scalalogging.StrictLogging
import commands.ClientCommands._
import commands.entities._
import commands.executor.CommandExecutor

import scala.io.StdIn.readLine
import scala.util.Try

class ClientCommandsLoop(executor: CommandExecutor) extends StrictLogging {

  def start(): Unit = {
    println(
      "Hey, welcome to fun-chat! \n" +
        "Type 'help' to show demo client commands.")

    Try {
      do {
        val input: String = readLine()
        input match {
          case cmd if cmd.startsWith(HELP)               => executor.execute(HelpCommand(cmd))
          case cmd if cmd.startsWith(SIGN_IN)            => executor.execute(SignInCommand(cmd))
          case cmd if cmd.startsWith(SIGN_UP)            => executor.execute(SignUpCommand(cmd))
          case cmd if cmd.startsWith(SIGN_OUT)           => executor.execute(SignOutCommand(cmd))
          case cmd if cmd.startsWith(UPDATE_CREDENTIALS) => executor.execute(UpdateCredentialsCommand(cmd))
          case cmd if cmd.startsWith(ONLINE_USERS)       => executor.execute(ListOnlineUsersCommand(cmd))
          case cmd if cmd.startsWith(USER_INFO)          => executor.execute(GetUserInfoCommand(cmd))
          case cmd if cmd.startsWith(SEND_MESSAGE)       => executor.execute(SendMessageCommand(cmd))
          case cmd if cmd.startsWith(EXIT)               => executor.execute(ExitCommand(cmd))
          case other                                     => logger.info(s"Unsupported command ! $other")
        }
      } while (true)
    }.recover {
      case ex: Exception =>
        logger.error("Unexpected error occurred!", ex)
    }
  }
}
