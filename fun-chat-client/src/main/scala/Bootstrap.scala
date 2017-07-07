import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import authentication.Authenticator
import authentication.fsm.AuthenticationFSM
import commands.executor.CommandExecutor
import commands.parser.CommandParser
import commands.runner.ClientCommandsLoop
import rest.client.HttpRestClient
import utils.Configuration

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}

class Bootstrap() {

  def startup(): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem("fun-chat-client")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext            = actorSystem.dispatcher

    val config = new Configuration()

    val restClient                  = new HttpRestClient(config)
    val authenticator: ActorRef     = actorSystem.actorOf(Authenticator.props(restClient), "authenticator")
    val authenticationFSM: ActorRef = actorSystem.actorOf(AuthenticationFSM.props(authenticator), "authenticationFSM")

    val commandParser = new CommandParser()
    val messenger: ActorRef = null
    val clientCommandsExecutor: ActorRef =
      actorSystem.actorOf(CommandExecutor.props(commandParser, authenticationFSM, restClient, messenger),
                          "command-executor")

    val exitCallback = (exitCode: Int) => {
      val whenTerminated = actorSystem.terminate()
      Await.result(whenTerminated, Duration(30, SECONDS))
      System.exit(exitCode)
    }

    val clientCommandsLoop = new ClientCommandsLoop(clientCommandsExecutor, exitCallback)
    clientCommandsLoop.start()
  }
}
