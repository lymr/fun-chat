package restapi.http.routes.entities

import core.entities.User
import org.joda.time.DateTime

private[http] case class UserInformationEntity(name: String, lastSeen: DateTime)

object UserInformationEntity {

  def fromUser(user: User): UserInformationEntity =
    UserInformationEntity(user.name, user.lastSeen)
}
