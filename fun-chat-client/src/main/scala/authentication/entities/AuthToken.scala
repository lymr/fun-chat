package authentication.entities

abstract class AuthToken

case class BearerToken(token: String) extends AuthToken
