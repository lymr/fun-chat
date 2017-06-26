package rest.client

import api.entities.UserInformationEntity
import authentication.entities.AuthToken

import scala.concurrent.Future

trait RestClient {

  def signIn(username: String, password: String): Future[AuthToken]

  def signUp(username: String, password: String): Future[AuthToken]

  def signOut(): Future[Int]

  def updateCredentials(newPassword: String): Future[Int]

  def listOnlineUsers(): Future[Seq[UserInformationEntity]]

  def findUserInformation(userName: String): Future[UserInformationEntity]

  def removeUser(): Future[Int]
}
