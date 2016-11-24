import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import core.db.{DatabaseService, FlywayService}
import utils.Configuration

import scala.concurrent.ExecutionContext

class Bootstrap {

  def startup(): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = actorSystem.dispatcher

    val config = new Configuration()

    val flywayService = new FlywayService(config)
    flywayService.migrateDatabaseSchema()

    val databaseService = new DatabaseService()
  }
}
