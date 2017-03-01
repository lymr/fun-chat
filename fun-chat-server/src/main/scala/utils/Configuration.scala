package utils

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

class Configuration {

  private val config         = ConfigFactory.load()
  private val httpConfig     = config.getConfig("http")
  private val databaseConfig = config.getConfig("production.db.default")
  private val authorization  = config.getConfig("authorization")
  private val messages       = config.getConfig("messages")

  val httpHost: String = httpConfig.getString("interface")
  val httpPort: Int    = httpConfig.getInt("port")

  val dbUrl: String      = databaseConfig.getString("url")
  val dbUser: String     = databaseConfig.getString("user")
  val dbPassword: String = databaseConfig.getString("password")

  val tokenExpiration: Long = authorization.getDuration("token-expiration").getSeconds

  val messageTimeout: FiniteDuration =
    FiniteDuration(messages.getDuration("message-timeout").getSeconds, SECONDS)
}
