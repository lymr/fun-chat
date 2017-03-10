package messages.entities

abstract class ClientRequest

object ListOnlineUsers extends ClientRequest

case class GetUserInformation(userName: String) extends ClientRequest

case class DeliverMessage(content: String) extends ClientRequest
