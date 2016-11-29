package core.db.users

import java.util.UUID

import core.db.PostgreSQLExtensions
import core.entities.CredentialSet
import core.entities.Defines.{UserID, UserSecret}
import scalikejdbc._

case class CredentialsDaoEntity(userId: UserID, password: Array[Char], slat: Array[Char], algorithm: Array[Char])

object CredentialsDaoEntity extends SQLSyntaxSupport[CredentialsDaoEntity] {
  override def tableName: String = "user_credentials"

  def apply(uc: SyntaxProvider[CredentialsDaoEntity])(rs: WrappedResultSet): CredentialsDaoEntity =
    apply(uc.resultName)(rs)

  def apply(uc: ResultName[CredentialsDaoEntity])(rs: WrappedResultSet): CredentialsDaoEntity =
    CredentialsDaoEntity(rs.string(uc.userId),
                         rs.string(uc.password).toCharArray,
                         rs.string(uc.slat).toCharArray,
                         rs.string(uc.algorithm).toCharArray)
}

class SqlUserCredentialsDao(credentialGenerator: (Array[Char]) => Option[CredentialSet])
    extends UserCredentialsDao with PostgreSQLExtensions {

  val c = CredentialsDaoEntity.syntax("uc")

  override def findUserCredentials(userId: UserID)(implicit session: DBSession): Option[CredentialSet] = {
    val id = UUID.fromString(userId)
    withSQL {
      select(c.result.password).from(CredentialsDaoEntity as c).where.eq(c.userId, id)
    }.map(CredentialsDaoEntity(c)).single().apply().map(toCredentialSet(_))
  }

  override def updateUserCredentials(userId: UserID, password: UserSecret)(implicit session: DBSession): Unit = {
    val credentials = credentialGenerator(password.toCharArray)
      .getOrElse(throw new RuntimeException("Failed to generate credential."))

    val id  = UUID.fromString(userId)
    val ucc = CredentialsDaoEntity.column
    withSQL {
      insert
        .into(CredentialsDaoEntity)
        .namedValues(ucc.userId    -> id,
                     ucc.password  -> credentials.password,
                     ucc.slat      -> credentials.salt,
                     ucc.algorithm -> credentials.algorithm)
    }.update().apply()
  }

  private def toCredentialSet(entity: CredentialsDaoEntity): CredentialSet = {
    CredentialSet(entity.password, entity.slat, entity.algorithm)
  }
}
