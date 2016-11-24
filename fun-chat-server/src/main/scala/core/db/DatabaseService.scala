package core.db

import core.db.users.{SqlUsersDao, UsersDao}
import scalikejdbc.config.DBs

class DatabaseService extends DatabasePrerequisite {
  val usersDao: UsersDao = new SqlUsersDao()
}

trait DatabasePrerequisite {
  DBs.setupAll() // startup jdbc
}
