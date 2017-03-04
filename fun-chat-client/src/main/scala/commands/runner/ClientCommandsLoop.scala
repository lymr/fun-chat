package commands.runner

import com.typesafe.scalalogging.StrictLogging
import commands.ClientCommands._
import commands._
import rest.client.RestClient

import scala.io.StdIn.readLine
import scala.util.Try

class ClientCommandsLoop(restClient: RestClient, exitCallback: (Int) => Unit) extends StrictLogging {

  def start(): Unit = {
    println(
      "Hey, welcome to fun-chat! \n" +
        "Type 'help' to show demo client commands.")

    Try {
      do {
        val input: String = readLine()
        input match {
          case cmd if cmd.startsWith(HELP)         => HelpCommand.execute()
          case cmd if cmd.startsWith(SIGN_IN)      => SignInCommand(restClient).execute()
          case cmd if cmd.startsWith(SIGN_UP)      => SignUpCommand(restClient).execute()
          case cmd if cmd.startsWith(SIGN_OUT)     => println("Method not implemented!")
          case cmd if cmd.startsWith(ONLINE_USERS) => println("Method not implemented!")
          case cmd if cmd.startsWith(SEND_MESSAGE) => println("Method not implemented!")
          case cmd if cmd.startsWith(EXIT)         => exitCallback(0)
          case other                               => logger.info(s"Unsupported command ! $other")
        }
      } while (true)
    }.recover {
      case ex: Exception =>
        logger.error("Unexpected error occurred! exits..", ex)
        exitCallback(1)
    }
  }
}
