package websocket

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Props, Terminated}
import core.entities.UserID
import websocket.ConnectedClientsStore._

/**
  * Stores user's connected clients endpoint.
  */
class ConnectedClientsStore() extends Actor with ActorLogging {

  var clientsEndpoints: Map[UserID, ActorRef] = Map.empty
  var userEndpoints: Map[ActorPath, UserID]   = Map.empty

  override def receive: Receive = {

    case FindClient(userId) =>
      sender ! clientsEndpoints.get(userId).fold[ClientStoreResponse](ClientNotFound)(ClientFound)

    case ClientConnected(userId, clientEndpoint) =>
      clientsEndpoints += userId           -> clientEndpoint
      userEndpoints += clientEndpoint.path -> userId
      context.watch(clientEndpoint)

    case ClientDisconnected(userId) =>
      clientsEndpoints.get(userId).foreach(context.stop)

    case Terminated(clientEndpoint) =>
      userEndpoints.get(clientEndpoint.path).foreach { userId =>
        clientsEndpoints -= userId
        userEndpoints -= clientEndpoint.path
      }
  }
}

object ConnectedClientsStore {

  def props: Props = Props(new ConnectedClientsStore)

  case class ClientConnected(userId: UserID, clientEndpoint: ActorRef)
  case class ClientDisconnected(userId: UserID)
  case class FindClient(userId: UserID)

  trait ClientStoreResponse
  case object ClientNotFound extends ClientStoreResponse
  case class ClientFound(endpoint: ActorRef) extends ClientStoreResponse
}
