import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import core.authentication._
import core.db.{DatabaseContext, FlywayService}
import restapi.http.HttpService
import utils.Configuration

import scala.concurrent.ExecutionContext

class Bootstrap {

  def startup(): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = actorSystem.dispatcher

    val config = new Configuration()
    val dbc = new DatabaseContext()
    val flywayService = new FlywayService(config)
    flywayService.migrateDatabaseSchema()

    val userAuthenticator =
      new UserAuthenticator(SecretKeyHashUtils.validate, SecuredTokenGenerator.generateString, dbc.credentialsDao)

    val authService = new AuthenticationService(dbc.usersDao, userAuthenticator)
    val httpService = new HttpService(authService, dbc.usersDao, config)
    httpService.start()
  }
}
