package core.entities

case class TokenContext(userId: UserID, username: String)

object TokenContext {

  def fromUser(user: User): TokenContext = TokenContext(user.userId, user.name)
}
