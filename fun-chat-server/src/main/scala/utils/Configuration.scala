package utils

import com.typesafe.config.ConfigFactory

class Configuration {

  private val config         = ConfigFactory.load()
  private val httpConfig     = config.getConfig("http")
  private val databaseConfig = config.getConfig("production.db.default")
  private val authorization  = config.getConfig("authorization")

  val httpHost: String = httpConfig.getString("interface")
  val httpPort: Int    = httpConfig.getInt("port")

  val dbUrl: String      = databaseConfig.getString("url")
  val dbUser: String     = databaseConfig.getString("user")
  val dbPassword: String = databaseConfig.getString("password")

  val tokenExpiration: Long = authorization.getDuration("token-expiration").getSeconds
}
