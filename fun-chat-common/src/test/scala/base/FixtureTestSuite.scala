package base

import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, Matchers, fixture}
import scalikejdbc.config.DBsWithEnv
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc.{ConnectionPool, GlobalSettings}

trait FixtureTestSuite extends fixture.FunSuiteLike with AutoRollback with Matchers with MockitoSupport with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()
    GlobalSettings.jtaDataSourceCompatible = true
    DBsWithEnv("test").setupAll()

    val flyway = new Flyway()
    flyway.setDataSource(ConnectionPool().dataSource)
    flyway.clean()
    flyway.migrate()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    DBsWithEnv("test").closeAll()
  }
}
