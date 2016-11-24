package core.db.users

import core.entities.User
import core.entities.User.UserID
import scalikejdbc._

trait UsersDao {

  def createUser(name: String, password: String)(implicit session: DBSession = AutoSession): User

  def updateUserCredentials(user: User)(implicit session: DBSession = AutoSession): User

  def updateUserSessionTimestamp(user: User)(implicit session: DBSession = AutoSession): User

  def findUserByName(name: String)(implicit session: DBSession = AutoSession): Option[User]

  def findUserByID(userId: String)(implicit session: DBSession = AutoSession): Option[User]

  def deleteUser(user: User)(implicit session: DBSession = AutoSession): UserID
}
