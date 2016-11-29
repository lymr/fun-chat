package core.db.users

import java.util.UUID

import core.db.PostgreSQLExtensions
import core.entities.User
import core.entities.Defines._
import org.joda.time.DateTime
import scalikejdbc._

case class UserDaoEntity(userId: Option[UserID], userName: String, lastSeen: DateTime)

object UserDaoEntity extends SQLSyntaxSupport[UserDaoEntity] {
  override def tableName: String = "users"

  def apply(u: SyntaxProvider[UserDaoEntity])(rs: WrappedResultSet): UserDaoEntity = apply(u.resultName)(rs)

  def apply(u: ResultName[UserDaoEntity])(rs: WrappedResultSet): UserDaoEntity =
    UserDaoEntity(rs.stringOpt(u.userId), rs.string(u.userName), rs.jodaDateTime(u.lastSeen))
}

class SqlUsersDao(updateCredentialsOp: (UserID, UserSecret) => Unit) extends UsersDao with PostgreSQLExtensions {

  val u = UserDaoEntity.syntax("u")

  override def createUser(name: String, password: String)(implicit session: DBSession): User = {
    val id: UUID              = UUID.randomUUID()
    val currentTime: DateTime = DateTime.now
    val uc                    = UserDaoEntity.column
    withSQL {
      insert.into(UserDaoEntity).namedValues(uc.userId -> id, uc.userName -> name, uc.lastSeen -> currentTime)
    }.update().apply()

    updateCredentialsOp(id.toString, password)
    User(Some(id.toString), name, currentTime)
  }

  override def findUsers()(implicit session: DBSession): Seq[User] = {
    withSQL {
      select(u.result.*).from(UserDaoEntity as u)
    }.map(UserDaoEntity(u)).list().apply().map(toUser)
  }

  override def findUserByName(name: String)(implicit session: DBSession): Option[User] = {
    withSQL {
      select(u.result.*).from(UserDaoEntity as u).where.eq(u.userName, name)
    }.map(UserDaoEntity(u)).single().apply().map(toUser)
  }

  override def findUserByID(userId: String)(implicit session: DBSession): Option[User] = {
    val id: UUID = UUID.fromString(userId)
    withSQL {
      select(u.result.*).from(UserDaoEntity as u).where.eq(u.userId, id)
    }.map(UserDaoEntity(u)).single().apply().map(toUser)
  }

  override def updateUserLastSeen(userID: UserID, timestamp: DateTime)(implicit session: DBSession): Unit = {
    val id: UUID = UUID.fromString(userID)
    val uc       = UserDaoEntity.column
    withSQL {
      update(UserDaoEntity).set(uc.lastSeen -> timestamp.getMillis).where.eq(uc.userId, id)
    }.update().apply()
  }

  override def deleteUser(userId: String)(implicit session: DBSession): UserID = {
    val id: UUID = UUID.fromString(userId)
    val uc       = UserDaoEntity.column
    withSQL {
      delete.from(UserDaoEntity).where.eq(uc.userId, id)
    }.update().apply()
    userId
  }

  private def toUser(ue: UserDaoEntity): User =
    User(ue.userId, ue.userName, ue.lastSeen)
}
