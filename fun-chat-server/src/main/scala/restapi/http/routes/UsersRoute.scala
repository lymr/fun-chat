package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import core.db.users.UsersDao
import restapi.http.JsonSupport
import spray.json._

import scala.concurrent.ExecutionContext

class UsersRoute(usersDao: UsersDao)(implicit ec: ExecutionContext) extends Directives with JsonSupport {

  val route: Route = pathPrefix("users") {
    get {
      pathEndOrSingleSlash {
        complete(usersDao.findUsers())
      } ~
        parameter("name") { name =>
          val maybeUser = usersDao.findUserByName(name)
          maybeUser match {
            case Some(user) => complete(user.toJson)
            case None       => complete(StatusCodes.NotFound)
          }
        }
    }
  }
}
