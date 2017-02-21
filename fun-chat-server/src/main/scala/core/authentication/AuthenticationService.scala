package core.authentication

import akka.Done
import core.db.clients.ConnectedClientsStore
import core.db.users.UsersDao
import core.entities.Defines._
import core.entities.{TokenContext, UserID}
import restapi.http.entities.ClientInformation

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationService(authenticator: UserAuthenticator, dao: UsersDao, connectedClients: ConnectedClientsStore)(
    implicit ec: ExecutionContext) {

  def signIn(username: String, password: UserSecret, info: ClientInformation): Future[Option[AuthToken]] = Future {
    val tuple = for {
      user  <- dao.findUserByName(username)
      token <- authenticator.authenticate(user, password)
    } yield (user.userId, token)

    tuple.map {
      case (userId, token) => connectedClients.update(userId, info); token
    }
  }

  def signUp(username: String, password: UserSecret, info: ClientInformation): Future[Option[AuthToken]] = Future {
    val createUser: (String, String) => Option[AuthToken] =
      (unm, pss) => {
        val user  = dao.createUser(unm, pss)
        val token = authenticator.authenticate(user, password)
        connectedClients.update(user.userId, info)
        token
      }

    dao.findUserByName(username) match {
      case Some(_) => None
      case None    => createUser(username, password)
    }
  }

  def signOut(userId: UserID): Future[Done] = Future {
    connectedClients.remove(userId)
    Done
  }

  def authorize(token: AuthToken): Future[Option[TokenContext]] = Future {
    authenticator.validateToken(token).filter {
      case TokenContext(id, _) => connectedClients.isOnline(id)
    }
  }

  def updateCredentials(userId: UserID, username: String, password: String): Future[Option[AuthToken]] = Future {
    val updateUser: (UserID, String, String) => Option[AuthToken] =
      (uid, unm, pss) => {
        val user = dao.updateUser(uid, unm, pss)
        authenticator.authenticate(user, password)
      }

    dao.findUserByID(userId) match {
      case Some(_) => updateUser(userId, username, password)
      case None    => None
    }
  }
}
