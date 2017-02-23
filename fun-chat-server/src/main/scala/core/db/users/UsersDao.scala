package core.db.users

import core.entities.{User, UserID, UserSecret}
import org.joda.time.DateTime
import scalikejdbc._

trait UsersDao {

  def createUser(name: String, secret: UserSecret)(implicit session: DBSession = AutoSession): User

  def findUsers()(implicit session: DBSession = AutoSession): Seq[User]

  def findUserByName(name: String)(implicit session: DBSession = AutoSession): Option[User]

  def findUserByID(userId: UserID)(implicit session: DBSession = AutoSession): Option[User]

  def updateUser(userId: UserID, name: String, secret: UserSecret)(implicit session: DBSession = AutoSession): User

  def updateUserLastSeen(userId: UserID, timestamp: DateTime)(implicit session: DBSession = AutoSession): Unit

  def deleteUser(userId: UserID)(implicit session: DBSession = AutoSession): UserID
}
