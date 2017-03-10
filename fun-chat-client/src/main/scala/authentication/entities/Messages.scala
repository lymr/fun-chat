package authentication.entities

abstract class AuthRequest

case object SignOut extends AuthRequest

final case class SignIn(username: String, password: String) extends AuthRequest

final case class SignUp(username: String, password: String) extends AuthRequest

final case class UpdateCredentials(password: String) extends AuthRequest

abstract class AuthResponse

case object Authenticated extends AuthResponse

case object Disconnected extends AuthResponse

case class AuthFailure(error: Throwable) extends AuthResponse
