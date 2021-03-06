package core.db.users

import java.util.UUID

import core.db.PostgreSQLExtensions
import core.entities.{User, UserID, UserSecret}
import org.joda.time.DateTime
import scalikejdbc._

case class UserDaoEntity(userId: String, userName: String, lastSeen: DateTime)

object UserDaoEntity extends SQLSyntaxSupport[UserDaoEntity] {
  override def tableName: String = "users"

  def apply(u: SyntaxProvider[UserDaoEntity])(rs: WrappedResultSet): UserDaoEntity = apply(u.resultName)(rs)

  def apply(u: ResultName[UserDaoEntity])(rs: WrappedResultSet): UserDaoEntity =
    UserDaoEntity(rs.string(u.userId), rs.string(u.userName), rs.jodaDateTime(u.lastSeen))
}

class SqlUsersDao(credentialsDao: UserCredentialsDao) extends UsersDao with PostgreSQLExtensions {

  val u = UserDaoEntity.syntax("u")

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

  override def findUserByID(userId: UserID)(implicit session: DBSession): Option[User] = {
    val id: UUID = UUID.fromString(userId.id)
    withSQL {
      select(u.result.*).from(UserDaoEntity as u).where.eq(u.userId, id)
    }.map(UserDaoEntity(u)).single().apply().map(toUser)
  }

  override def createUser(name: String, password: UserSecret): User = DB localTx { implicit session =>
    val id: UUID              = UUID.randomUUID()
    val currentTime: DateTime = DateTime.now
    val uc                    = UserDaoEntity.column
    withSQL {
      insert.into(UserDaoEntity).namedValues(uc.userId -> id, uc.userName -> name, uc.lastSeen -> currentTime)
    }.update().apply()

    val userID = UserID(id.toString)

    credentialsDao.createUserCredentials(userID, password)
    User(userID, name, currentTime)
  }

  override def updateUser(userId: UserID, secret: UserSecret): Unit = DB localTx { implicit session =>
    val id: UUID              = UUID.fromString(userId.id)
    val currentTime: DateTime = DateTime.now
    val uc                    = UserDaoEntity.column
    withSQL {
      update(UserDaoEntity).set(uc.lastSeen -> currentTime).where.eq(uc.userId, id)
    }.update().apply()

    credentialsDao.updateUserCredentials(userId, secret)
  }

  override def updateUserLastSeen(userId: UserID, timestamp: DateTime)(implicit session: DBSession): Unit = {
    val id: UUID = UUID.fromString(userId.id)
    val uc       = UserDaoEntity.column
    withSQL {
      update(UserDaoEntity).set(uc.lastSeen -> timestamp.getMillis).where.eq(uc.userId, id)
    }.update().apply()
  }

  override def deleteUser(userId: UserID)(implicit session: DBSession): UserID = {
    val id: UUID = UUID.fromString(userId.id)
    val uc       = UserDaoEntity.column
    withSQL {
      delete.from(UserDaoEntity).where.eq(uc.userId, id)
    }.update().apply()
    userId
  }

  private def toUser(ue: UserDaoEntity): User =
    User(UserID(ue.userId), ue.userName, ue.lastSeen)
}
