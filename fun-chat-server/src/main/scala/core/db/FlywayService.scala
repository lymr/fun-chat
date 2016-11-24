package core.db

import org.flywaydb.core.Flyway
import utils.Configuration

class FlywayService(configuration: Configuration) {

  private val flyway = new Flyway()
  flyway.setDataSource(configuration.dbUrl, configuration.dbUser, configuration.dbPassword)

  def migrateDatabaseSchema(): Unit = flyway.migrate()

  def dropDatabase(): Unit = flyway.clean()
}
