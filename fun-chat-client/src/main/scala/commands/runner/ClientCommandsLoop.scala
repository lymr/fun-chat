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
          case HELP         => HelpCommand.execute()
          case SIGN_IN      => SignInCommand(restClient).execute()
          case SIGN_UP      => SignUpCommand(restClient).execute()
          case SIGN_OUT     => println("Method not implemented!")
          case ONLINE_USERS => println("Method not implemented!")
          case SEND_MESSAGE => println("Method not implemented!")
          case EXIT         => exitCallback(0)
          case other        => logger.info(s"Unsupported command ! $other")
        }
      } while (true)
    }.recover {
      case ex: Exception =>
        logger.error("Unexpected error occurred! exits..", ex)
        exitCallback(1)
    }
  }
}
