import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import authentication.Authenticator
import authentication.fsm.AuthenticationFSM
import commands.entities.ClientCommand
import commands.executor.CommandExecutor
import commands.parser.CommandParser
import commands.runner.ClientCommandsLoop
import rest.client.HttpRestClient
import utils.Configuration

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}

class Bootstrap() {

  def startup(): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext            = actorSystem.dispatcher

    val config = new Configuration()

    val restClient              = new HttpRestClient(config)
    val authenticator: ActorRef = actorSystem.actorOf(Authenticator.props(restClient), "authenticator")
    val authenticationFSM: ActorRef =
      actorSystem.actorOf(AuthenticationFSM.props(authenticator), "authenticationFSM")

    val onFailureCallback = (cmd: ClientCommand, ex: Throwable) =>
      println(s"Failed executing command $cmd with error:= ${ex.getMessage}")

    val exitCallback = (exitCode: Int) => {
      val whenTerminated = actorSystem.terminate()
      Await.result(whenTerminated, Duration(30, SECONDS))
      System.exit(exitCode)
    }

    val commandParser: CommandParser = new CommandParser()
    val messanger: ActorRef          = null //TODO: create executor
    val clientCommandsExecutor: ActorRef =
      actorSystem.actorOf(CommandExecutor.props(commandParser, authenticationFSM, messanger), "command-executor")

    val clientCommandsLoop = new ClientCommandsLoop(clientCommandsExecutor, exitCallback)
    clientCommandsLoop.start()
  }
}
