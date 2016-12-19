package core.db.users

import core.entities.CredentialSet
import core.entities.Defines.{UserID, UserSecret}
import scalikejdbc._

trait UserCredentialsDao {

  def findUserCredentials(userId: UserID)(implicit session: DBSession = AutoSession): Option[CredentialSet]

  def createUserCredentials(userId: UserID, password: UserSecret)(implicit session: DBSession = AutoSession): Unit

  def updateUserCredentials(userId: UserID, password: UserSecret)(implicit session: DBSession = AutoSession): Unit
}


