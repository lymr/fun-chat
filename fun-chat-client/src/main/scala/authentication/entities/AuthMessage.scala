package authentication.entities

abstract class AuthMessage

case object SignOut extends AuthMessage

case class SignIn(username: String, password: String) extends AuthMessage

case class SignUp(username: String, password: String) extends AuthMessage

case class ProcessingDone(result: AuthResult) extends AuthMessage
