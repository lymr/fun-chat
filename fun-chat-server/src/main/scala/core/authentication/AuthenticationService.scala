package core.authentication

import akka.Done
import core.db.users.UsersDao
import core.entities.Defines._
import core.entities.User

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationService(authManager: UserAuthenticator, val dao: UsersDao)(
    implicit ec: ExecutionContext) {

  def signIn(login: String, password: UserSecret): Future[Option[AuthToken]] = Future {
    dao.findUserByName(login) match {
      case Some(User(Some(id: UserID), _, _)) => authManager.authenticate(id, password)
      case _                                  => None
    }
  }

  def signOut(login: String, token: AuthToken): Future[Done] = Future {
    dao.findUserByName(login).foreach(authManager.revoke(_, token))
    Done
  }

  def signUp(login: String, password: UserSecret): Future[Option[AuthToken]] = Future {
    val createUser: (String, String) => Option[AuthToken] =
      (l, p) => dao.createUser(l, p).userId match {
        case Some(id) => authManager.authenticate(id, password)
        case None     => None
      }

    dao.findUserByName(login).fold(createUser(login, password))(_ => None)
  }

  def authorize(userId: UserID, token: AuthToken): Boolean = {
    authManager.validateToken(userId, token)
  }
}

