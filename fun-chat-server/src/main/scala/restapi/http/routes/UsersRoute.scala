package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import core.db.users.UsersDao
import core.entities.UserInformationEntityConverters._
import restapi.http.JsonSupport
import restapi.http.routes.support.SecuredAccessSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

class UsersRoute(usersDao: UsersDao)(implicit ec: ExecutionContext, ac: ApiContext)
    extends Directives with SecuredAccessSupport with JsonSupport {

  val route: Route = pathPrefix("users") {
    securedAccess { ctx =>
      pathEndOrSingleSlash {
        get {
          complete(usersDao.findUsers().map(toUserInformationEntity))
        }
      } ~
        pathPrefix("name" / Segment) { name =>
          pathEndOrSingleSlash {
            get {
              complete {
                val maybeUser = usersDao.findUserByName(name)
                maybeUser match {
                  case Some(user) => toUserInformationEntity(user)
                  case None       => StatusCodes.NotFound
                }
              }
            } ~
              delete {
                privateResourceAccess(ctx, name) {
                  complete {
                    Try(usersDao.deleteUser(ctx.userId))
                    StatusCodes.OK
                  }
                }
              }
          }
        }
    }
  }

}
