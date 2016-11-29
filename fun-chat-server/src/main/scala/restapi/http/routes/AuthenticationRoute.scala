package restapi.http.routes

import akka.http.scaladsl.coding.Deflate
import akka.http.scaladsl.server.Directives
import core.db.users.UsersDao
import restapi.http.JsonSupport

import scala.concurrent.ExecutionContext

class AuthenticationRoute(usersDao: UsersDao)(implicit ec: ExecutionContext) extends Directives with JsonSupport {

  val route = {
    path("users") {
//      authenticateBasic(realm = "user area", authenticator) { user =>
      get {
        encodeResponseWith(Deflate) {
          complete {
            usersDao.findUsers()
          }
        }
      }
    }
  }
//  }
}
