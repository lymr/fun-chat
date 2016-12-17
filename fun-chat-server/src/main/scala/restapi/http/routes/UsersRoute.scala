package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import core.db.users.UsersDao
import restapi.http.JsonSupport
import restapi.http.entities._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class UsersRoute(usersDao: UsersDao)(implicit ec: ExecutionContext, ac: AuthorizationContext)
    extends Directives
    with SecuredAccessSupport
    with JsonSupport {

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
              delete {
                privateResourceAccess(ctx, name) {
                  Try(usersDao.deleteUser(ctx.userId.get)) match {
                    case Success(_)  => complete(StatusCodes.OK)
                    case Failure(ex) => complete(StatusCodes.NotFound, ex.getMessage)
                  }
                }
              }
          }
        }
    }
  }

}
