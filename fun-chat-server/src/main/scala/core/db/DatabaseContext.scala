package core.db

import core.authentication.UserSecretUtils
import core.authentication.tokenGenerators.SecuredTokenGenerator
import core.db.users._
import scalikejdbc.config.DBsWithEnv

class DatabaseContext extends DatabasePrerequisite {
  val credentialsDao: UserCredentialsDao = new SqlUserCredentialsDao(
    UserSecretUtils.encrypt(_, SecuredTokenGenerator.generate))

  val usersDao: UsersDao = new SqlUsersDao(credentialsDao)
}

trait DatabasePrerequisite {
  DBsWithEnv("production").setupAll()
}
