package authentication.fsm.entities

abstract class AuthResult

case object Processing extends AuthResult

case object Success extends AuthResult

case object Failure extends AuthResult
