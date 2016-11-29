package core.authentication

import core.db.users.UserCredentialsDao
import core.entities.Defines._
import core.entities.{CredentialSet, User}

class UserAuthenticator(secretValidator: (CredentialSet, String) => Boolean,
                        tokenGenerator: () => String,
                        credentialsDao: UserCredentialsDao) {

  private var tokenStore: Map[UserID, AuthToken] = Map.empty

  def authenticate(userID: UserID, password: UserSecret): Option[AuthToken] = {
    val savedSecret = credentialsDao.findUserCredentials(userID)
    savedSecret.map {
      case storedSecret if secretValidator(storedSecret, password) => createToken(userID)
    }
  }

  def revoke(user: User): Unit = {
    user.userId.filter(tokenStore.contains).foreach(remove)
  }

  def validateToken(userID: UserID, token: AuthToken): Boolean = {
    tokenStore.get(userID).fold(ifEmpty = false)(_ == token)
  }

  private def createToken(id: UserID): AuthToken = {
    tokenStore.get(id) match {
      case Some(token) => token
      case None        => add(id)
    }
  }

  private def add(id: UserID): AuthToken = {
    val token = tokenGenerator()
    tokenStore = tokenStore + (id -> token)
    token
  }

  private def remove(id: UserID): Unit = {
    tokenStore = tokenStore - id
  }
}
