package authentication.fsm.entities

abstract class AuthToken

case class BearerToken(token: String) extends AuthToken
