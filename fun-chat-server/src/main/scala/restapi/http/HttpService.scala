package restapi.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import core.authentication.AuthenticationService
import core.db.users.UsersDao
import restapi.http.routes.AuthenticationRoute
import utils.Configuration

import scala.concurrent.ExecutionContext

class HttpService(authService: AuthenticationService, usersDao: UsersDao, config: Configuration)(
    implicit ec: ExecutionContext) extends StrictLogging {

  val authRoute = new AuthenticationRoute(usersDao)

  def start()(implicit actorSystem: ActorSystem, materializer: Materializer): Unit = {
    val bindingFuture = Http().bindAndHandle(authRoute.route, config.httpHost, config.httpPort)
    logger.info("HTTP service is up!")
  }
}
