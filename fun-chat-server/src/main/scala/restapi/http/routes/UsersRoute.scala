package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import core.db.users.UsersDao
import restapi.http.JsonSupport
import restapi.http.routes.entities._

import scala.concurrent.ExecutionContext

class UsersRoute(usersDao: UsersDao)(implicit ec: ExecutionContext) extends Directives with JsonSupport {

  val route: Route = pathPrefix("users") {
    pathEndOrSingleSlash {
      get {
        complete(usersDao.findUsers().map(UserInformationEntity.fromUser))
      }
    } ~
      pathPrefix("id" / Segment) { id =>
        pathEndOrSingleSlash {
          get {
            val maybeUser = usersDao.findUserByID(id)
            maybeUser match {
              case Some(user) => complete(UserInformationEntity.fromUser(user))
              case None       => complete(StatusCodes.NotFound)
            }
          }
          delete {
            complete(usersDao.deleteUser(id))
          }
        }
      } ~
      pathPrefix("name" / Segment) { name =>
        pathEndOrSingleSlash {
          get {
            val maybeUser = usersDao.findUserByName(name)
            maybeUser match {
              case Some(user) => complete(UserInformationEntity.fromUser(user))
              case None       => complete(StatusCodes.NotFound)
            }
          }
        }
      }
  }

}
