package core.authentication

import akka.Done
import core.db.users.UsersDao
import core.entities.Defines._
import core.entities.TokenContext

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationService(authenticator: UserAuthenticator, dao: UsersDao, addressStore: UsersAddressBookStore)(
    implicit ec: ExecutionContext) {

  def signIn(username: String, password: UserSecret): Future[Option[AuthToken]] = Future {
    dao.findUserByName(username).flatMap(user => authenticator.authenticate(user, password))
  }

  def signUp(username: String, password: UserSecret): Future[Option[AuthToken]] = Future {
    val createUser: (String, String) => Option[AuthToken] =
      (l, p) => {
        val user = dao.createUser(l, p)
        authenticator.authenticate(user, password)
      }

    dao.findUserByName(username) match {
      case Some(_) => None
      case None    => createUser(username, password)
    }
  }

  def signOut(userId: UserID): Future[Done] = Future {
    addressStore.remove(userId)
    Done
  }

  def authorize(token: AuthToken): Future[Option[TokenContext]] = Future {
    authenticator.validateToken(token).filter {
      case TokenContext(id, _) => addressStore.isOnline(id)
    }
  }
}
