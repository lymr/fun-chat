package core.authentication

import core.entities.User
import core.entities.User.{AuthToken, UserID}

class AuthenticationManager(secretValidator: (String, String) => Boolean,
                            tokenGenerator: () => String) {

  private var tokenStore: Map[UserID, AuthToken] = Map.empty

  def authenticate(user: User, secretKey: String): Option[AuthToken] = {
    user.userId match {
      case Some(id) if secretValidator(user.password, secretKey) => Some(createToken(id))
      case _ => None
    }
  }

  def revoke(user: User): Unit = {
    user.userId.filter(tokenStore.contains).foreach(remove)
  }

  def validateToken(user: User): Boolean = {
    user.userId match {
      case Some(id) => tokenStore.get(id) == user.authToken
      case None => false
    }
  }

  private def createToken(id: UserID): AuthToken = {
    tokenStore.get(id) match {
      case Some(token) => token
      case None => add(id)
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
