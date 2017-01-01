import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import commands.runner.ClientCommandsLoop

import scala.concurrent.ExecutionContext

class Bootstrap() {

  def startup(): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext            = actorSystem.dispatcher

    val exitCallback: (Int) => Unit = System.exit
    val clientCommandsLoop = new ClientCommandsLoop(exitCallback)
    clientCommandsLoop.start()
  }
}
