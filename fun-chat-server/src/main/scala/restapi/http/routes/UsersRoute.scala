package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import core.db.users.UsersDao
import restapi.http.JsonSupport
import restapi.http.entities._

import scala.concurrent.ExecutionContext
import scala.util.{Success, Try}

class UsersRoute(usersDao: UsersDao)(implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

  val route: Route = pathPrefix("users") {
    securedAccess { ctx =>
      pathEndOrSingleSlash {
        get {
          complete(usersDao.findUsers().map(UserInformationEntity.fromUser))
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
            } ~
              post {
                complete(StatusCodes.OK)
              } ~
              delete {
                privateResourceAccess(ctx, name) {
                  Try(usersDao.deleteUser(ctx.userId)) match {
                    case Success(_) => complete(StatusCodes.OK)
                    case _          => complete(StatusCodes.NotFound)
                  }
                }
              }
          }
        }
    }
  }

}
