package core.db

import core.authentication.{SecretKeyHashUtils, SecuredTokenGenerator}
import core.db.users._
import scalikejdbc.config.DBsWithEnv

class DatabaseContext extends DatabasePrerequisite {
  val credentialsDao: UserCredentialsDao = new SqlUserCredentialsDao(
    SecretKeyHashUtils.generate(_, SecuredTokenGenerator.generate()))

  val usersDao: UsersDao = new SqlUsersDao(credentialsDao.updateUserCredentials)
}

trait DatabasePrerequisite {
  DBsWithEnv("production").setupAll()
}
