package api.entities

/**
  * Authenticated client information
  * @param version client version
  * @param ipAddress client ip address for messaging
  */
case class ClientInformation(version: String, ipAddress: String)
