package restapi.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import restapi.http.routes.HttpRouter
import utils.Configuration

class HttpService(http: HttpRouter, config: Configuration) extends StrictLogging {

  def start()(implicit actorSystem: ActorSystem, materializer: Materializer): Unit = {
    val bindingFuture = Http().bindAndHandle(http.routes, config.httpHost, config.httpPort)
    logger.info("HTTP service is up!")
  }
}
