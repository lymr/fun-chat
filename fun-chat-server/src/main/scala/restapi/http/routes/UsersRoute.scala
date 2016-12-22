package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import core.db.users.UsersDao
import restapi.http.JsonSupport
import restapi.http.entities._
import restapi.http.routes.support.SecuredAccessSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

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
              complete {
                val maybeUser = usersDao.findUserByName(name)
                maybeUser match {
                  case Some(user) => UserInformationEntity.fromUser(user)
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
