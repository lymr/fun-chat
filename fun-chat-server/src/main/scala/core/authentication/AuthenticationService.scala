package core.authentication

import akka.Done
import api.entities.ClientInformation
import core.db.clients.ConnectedClientsStore
import core.db.users.UsersDao
import core.entities._

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

  def signUp(username: String, secret: UserSecret, info: ClientInformation): Future[Option[AuthToken]] = Future {
    val createUser: (String, UserSecret) => Option[AuthToken] =
      (unm, pss) => {
        val user  = dao.createUser(unm, pss)
        val token = authenticator.authenticate(user, secret)
        connectedClients.update(user.userId, info)
        token
      }

    dao.findUserByName(username) match {
      case Some(_) => None
      case None    => createUser(username, secret)
    }
  }

  def signOut(userId: UserID): Future[Done] = Future {
    connectedClients.remove(userId)
    Done
  }

  def authorize(token: AuthToken): Future[Option[AuthTokenContext]] = Future {
    authenticator.validateToken(token).filter {
      case AuthTokenContext(id, _) => connectedClients.isOnline(id)
    }
  }

  def updateCredentials(userId: UserID, newSecret: UserSecret): Future[Option[AuthToken]] = Future {
    val updateUser: (User, UserSecret) => Option[AuthToken] =
      (user, secret) => {
        dao.updateUser(user.userId, secret)
        authenticator.authenticate(user, secret)
      }

    dao.findUserByID(userId) match {
      case Some(user) => updateUser(user, newSecret)
      case None       => None
    }
  }
}
