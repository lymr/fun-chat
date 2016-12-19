package core.db

import core.authentication.SecretKeyHashUtils
import core.authentication.tokenGenerators.SecuredTokenGenerator
import core.db.users._
import scalikejdbc.config.DBsWithEnv

class DatabaseContext extends DatabasePrerequisite {
  val credentialsDao: UserCredentialsDao = new SqlUserCredentialsDao(
    SecretKeyHashUtils.generate(_, SecuredTokenGenerator.generate()))

  val usersDao: UsersDao = new SqlUsersDao(credentialsDao.createUserCredentials, credentialsDao.updateUserCredentials)
}

trait DatabasePrerequisite {
  DBsWithEnv("production").setupAll()
}
