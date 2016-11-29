package utils

import com.typesafe.config.ConfigFactory

class Configuration {

  private val config     = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val databaseConfig = config.getConfig("production.db.default")

  val httpHost = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val dbUrl      = databaseConfig.getString("url")
  val dbUser     = databaseConfig.getString("user")
  val dbPassword = databaseConfig.getString("password")
}
