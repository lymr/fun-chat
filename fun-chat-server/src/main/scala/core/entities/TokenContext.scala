package core.entities

import core.entities.Defines.UserID

case class TokenContext(userId: UserID, username: String)

object TokenContext {

  def fromUser(user: User): TokenContext = {
    require(user.userId.isDefined)
    TokenContext(user.userId.get, user.name)
  }
}
