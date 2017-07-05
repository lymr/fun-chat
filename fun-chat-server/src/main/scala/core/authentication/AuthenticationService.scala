package core.authentication

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import api.entities.ClientInformation
import core.db.users.UsersDao
import core.entities._
import websocket.ConnectedClientsStore.{ClientStoreResponse, _}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AuthenticationService(authenticator: UserAuthenticator, dao: UsersDao, connectedClientsStore: ActorRef) {

  implicit val askTimeout: Timeout = 2 seconds

  def signIn(username: String, password: UserSecret, info: ClientInformation)
            (implicit ec: ExecutionContext): Future[Option[AuthToken]] = Future {
    for {
      user  <- dao.findUserByName(username)
      token <- authenticator.authenticate(user, password)
    } yield token
  }

  def signUp(username: String, secret: UserSecret, info: ClientInformation)
            (implicit ec: ExecutionContext): Future[Option[AuthToken]] = Future {

    def createUser(name: String, password: UserSecret): Option[AuthToken] = {
      val user = dao.createUser(name, password)
      authenticator.authenticate(user, secret)
    }

    dao.findUserByName(username) match {
      case Some(_) => None
      case None    => createUser(username, secret)
    }
  }

  def signOut(userId: UserID)(implicit ec: ExecutionContext): Future[Done] = Future {
    connectedClientsStore ! ClientDisconnected(userId)
    Done
  }

  def authorize(token: AuthToken)(implicit ec: ExecutionContext): Future[Option[AuthTokenContext]] = {
    Future(authenticator.validateToken(token)).flatMap {
      case Some(userAuthContext) =>
        (connectedClientsStore ? ValidateUserClaims(userAuthContext))
          .mapTo[ClientStoreResponse]
          .map {
            case ClaimsValid                    => Some(userAuthContext)
            case ClaimsInvalid | ClientNotFound => None
          }
      case None => Future.successful(None)
    }
  }

  def updateCredentials(userId: UserID, newSecret: UserSecret)
                       (implicit ec: ExecutionContext): Future[Option[AuthToken]] = Future {

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
