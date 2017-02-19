import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import commands.runner.ClientCommandsLoop
import rest.client.HttpRestClient
import utils.Configuration

import scala.concurrent.ExecutionContext

class Bootstrap() {

  def startup(): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext            = actorSystem.dispatcher

    val config = new Configuration()

    val restClient         = new HttpRestClient(config)
    val clientCommandsLoop = new ClientCommandsLoop(restClient, System.exit)
    clientCommandsLoop.start()
  }
}
