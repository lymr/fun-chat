package core.entities

/**
  * Defines General Token*
  */
abstract class Token

abstract class AuthToken extends Token

case class AuthTokenContext(userId: UserID, username: String)

object AuthTokenContext {
  def fromUser(user: User): AuthTokenContext = AuthTokenContext(user.userId, user.name)
}

case class BearerToken(token: String) extends AuthToken

case class SecuredToken(token: Array[Byte]) extends Token
