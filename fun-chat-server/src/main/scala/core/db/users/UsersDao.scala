package core.db.users

import core.entities.User
import core.entities.Defines.UserID
import org.joda.time.DateTime
import scalikejdbc._

trait UsersDao {

  def createUser(name: String, password: String)(implicit session: DBSession = AutoSession): User

  def findUsers()(implicit session: DBSession = AutoSession): Seq[User]

  def findUserByName(name: String)(implicit session: DBSession = AutoSession): Option[User]

  def findUserByID(userId: String)(implicit session: DBSession = AutoSession): Option[User]

  def updateUserLastSeen(userID: UserID, timestamp: DateTime)(implicit session: DBSession = AutoSession): Unit

  def deleteUser(userId: UserID)(implicit session: DBSession = AutoSession): UserID
}
