package core.db.users

import java.util.UUID

import core.db.PostgreSQLExtensions
import core.entities.User
import core.entities.User.UserID
import org.joda.time.DateTime
import scalikejdbc._

case class UserDaoEntity(userId: Option[UserID], name: String, password: String, lastSession: DateTime)

object UserDaoEntity extends SQLSyntaxSupport[UserDaoEntity] {
  override def tableName: String = "users"

  def apply(u: SyntaxProvider[UserDaoEntity])(rs: WrappedResultSet): UserDaoEntity = apply(u.resultName)(rs)

  def apply(u: ResultName[UserDaoEntity])(rs: WrappedResultSet): UserDaoEntity =
    UserDaoEntity(rs.stringOpt(u.userId), rs.string(u.name), rs.string(u.password), rs.jodaDateTime(u.lastSession))
}

class SqlUsersDao extends UsersDao with PostgreSQLExtensions {

  val u = UserDaoEntity.syntax("u")

  override def createUser(name: String, password: String)(implicit session: DBSession): User = {
    val id: UUID = UUID.randomUUID()
    val sessionTimestamp: DateTime = DateTime.now
    val uc = UserDaoEntity.column
    withSQL {
      insert
        .into(UserDaoEntity)
        .namedValues(
          uc.userId -> id,
          uc.name -> name,
          uc.password -> password,
          uc.lastSession -> sessionTimestamp
        )
    }.update().apply()

    User(Some(id.toString), name, password, sessionTimestamp, None)
  }

  override def updateUserCredentials(user: User)(implicit session: DBSession): User = {
    val id: UUID = UUID.fromString(user.userId.get)
    val uc = UserDaoEntity.column
    withSQL {
      update(UserDaoEntity).set(uc.name -> user.name, uc.password -> user.password).where.eq(uc.userId, id)
    }.update().apply()
    user
  }

  override def updateUserSessionTimestamp(user: User)(implicit session: DBSession): User = {
    val id: UUID = UUID.fromString(user.userId.get)
    val uc = UserDaoEntity.column
    withSQL {
      update(UserDaoEntity).set(uc.lastSession -> user.lastSession).where.eq(uc.userId, id)
    }.update().apply()
    user
  }

  override def findUserByName(name: String)(implicit session: DBSession): Option[User] = {
    withSQL {
      select(u.result.*).from(UserDaoEntity as u).where.eq(u.name, name)
    }.map(UserDaoEntity(u)).single().apply().map(toUser)
  }

  override def findUserByID(userId: String)(implicit session: DBSession): Option[User] = {
    val id: UUID = UUID.fromString(userId)
    withSQL {
      select(u.result.*).from(UserDaoEntity as u).where.eq(u.userId, id)
    }.map(UserDaoEntity(u)).single().apply().map(toUser)
  }

  override def deleteUser(user: User)(implicit session: DBSession): UserID = {
    val id: UUID = UUID.fromString(user.userId.get)
    val uc = UserDaoEntity.column
    withSQL {
      delete.from(UserDaoEntity).where.eq(uc.userId, id)
    }.update().apply()
    user.userId.get
  }

  private def toUser(ue: UserDaoEntity): User =
    User(ue.userId, ue.name, ue.password, ue.lastSession, None)
}
