package core.authentication

import akka.Done
import core.authentication.AuthenticationService._
import core.db.users.UsersDao
import core.entities.Defines._
import core.entities.User

import scala.concurrent.Future

class AuthenticationService(val dao: UsersDao, val authManager: UserAuthenticator) {

  def signIn(login: String, password: UserSecret): Future[AuthToken] = {
    dao.findUserByName(login) match {
      case Some(User(Some(id: UserID), _, _)) => authManager.authenticate(id, password)
          .fold[Future[AuthToken]](Future.failed(withError(SIGN_IN_FAILURE, login)))(Future.successful)
      case _ => Future.failed(withError(SIGN_IN_FAILURE, login))
    }
  }

  def signOut(login: String, token: AuthToken): Future[Done] = {
    dao.findUserByName(login).foreach(authManager.revoke(_,token))
    Future.successful(Done)
  }

  def signUp(login: String, password: UserSecret): Future[AuthToken] = {
    dao.findUserByName(login) match {
      case Some(_) => Future.failed(withError(SIGN_UP_FAILURE, login))
      case None =>
        dao.createUser(login, password).userId match {
        case Some(id) => authManager.authenticate(id, password)
            .fold[Future[AuthToken]](Future.failed(withError(SIGN_UP_FAILURE, login)))(Future.successful)
        case None => Future.failed(withError(SIGN_UP_FAILURE, login))
      }
    }
  }

  private def withError(msg: String, args: String*): Exception =
    new Exception(msg.format(args))
}

object AuthenticationService {
  val SIGN_IN_FAILURE: String = s"User '%s' failed to sign-in."
  val SIGN_UP_FAILURE: String = s"User '%s' failed to sign-up."
}
