package utils

import com.typesafe.config.ConfigFactory

class Configuration {

  private val config            = ConfigFactory.load()
  private val clientInformation = config.getConfig("client-information")
  private val defaultServer     = config.getConfig("default-server")

  val clientVersion: String = clientInformation.getString("version")

  val defaultSeverHost: String = defaultServer.getString("host")
  val defaultSeverPort: Int    = defaultServer.getInt("port")
}
