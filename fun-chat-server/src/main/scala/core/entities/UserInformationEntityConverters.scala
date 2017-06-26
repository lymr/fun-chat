package core.entities

import api.entities.UserInformationEntity

object UserInformationEntityConverters {

  def toUserInformationEntity(user: User): UserInformationEntity =
    UserInformationEntity(user.name, user.lastSeen)

}
