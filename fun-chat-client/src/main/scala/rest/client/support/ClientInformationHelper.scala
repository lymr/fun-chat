package rest.client.support

import java.net.InetAddress

case object ClientInformationHelper {

  lazy val ClientIpAddress: String = getClientIpAddress()

  private def getClientIpAddress(): String = {
    val localhost = InetAddress.getLocalHost
    localhost.getHostAddress
  }
}
