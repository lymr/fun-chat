package core.authentication

import akka.Done
import core.db.users.UsersDao
import core.entities.User.AuthToken
import AuthenticationService._
import core.entities.User

import scala.concurrent.Future

class AuthenticationService(val dao: UsersDao, val authManager: AuthenticationManager) {

  def signIn(login: String, password: String): Future[AuthToken] = {
    val error: Exception = new Exception(SIGN_IN_FAILURE_MESSAGE.format(login))
    dao.findUserByName(login) match {
      case None => Future.failed(error)
      case Some(u: User) => authManager.authenticate(u, password)
        .fold[Future[AuthToken]](Future.failed(error))(Future.successful(_))
    }
  }

  def signOut(login: String): Future[Done] = {
    dao.findUserByName(login).foreach(authManager.revoke(_))
    Future.successful(Done)
  }

  def signUp(login: String, password: String): Future[AuthToken] = {
    val error: Exception = new Exception(SIGN_UP_FAILURE_MESSAGE.format(login))
    dao.findUserByName(login) match {
      case Some(_) => Future.failed(error)
      case None =>
        val user = dao.createUser(login, password)
        authManager.authenticate(user, password).fold[Future[AuthToken]](Future.failed(error))(Future.successful(_))
    }
  }
}

object AuthenticationService {
  val SIGN_IN_FAILURE_MESSAGE: String = s"User '%s' failed to sign-in."
  val SIGN_UP_FAILURE_MESSAGE: String = s"User '%s' failed to sign-up."
}
