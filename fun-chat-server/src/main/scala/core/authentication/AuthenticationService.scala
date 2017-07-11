package core.authentication

import akka.actor.ActorRef
import core.db.users.UsersDao
import core.entities._
import websocket.ConnectedClientsStore._

class AuthenticationService(authenticator: UserAuthenticator, dao: UsersDao, connectedClientsStore: ActorRef) {

  def signIn(username: String, password: UserSecret): Option[AuthToken] = {
    for {
      user  <- dao.findUserByName(username)
      token <- authenticator.authenticate(user, password)
    } yield token
  }

  def signUp(username: String, secret: UserSecret): Option[AuthToken] = {

    def createUser(name: String, password: UserSecret): Option[AuthToken] = {
      val user = dao.createUser(name, password)
      authenticator.authenticate(user, secret)
    }

    dao.findUserByName(username) match {
      case Some(_) => None
      case None    => createUser(username, secret)
    }
  }

  def signOut(userId: UserID): Unit = {
    connectedClientsStore ! ClientDisconnected(userId)
    authenticator.revokeToken(userId)
  }

  def authorize(token: AuthToken): Option[AuthTokenContext] = {
    authenticator.validateToken(token)
  }

  def updateCredentials(userId: UserID, newSecret: UserSecret): Option[AuthToken] = {

    def updateUser(user: User, secret: UserSecret): Option[AuthToken] = {
      dao.updateUser(user.userId, secret)
      authenticator.authenticate(user, secret)
    }

    dao.findUserByID(userId) match {
      case Some(user) => updateUser(user, newSecret)
      case None       => None
    }
  }
}
