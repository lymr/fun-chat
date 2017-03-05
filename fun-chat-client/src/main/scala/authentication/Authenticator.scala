package authentication

import akka.actor.{Actor, ActorLogging, Props}
import authentication.entities.{AuthToken, _}
import rest.client.RestClient
import rest.client.entities.ExecutionResultCode

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class Authenticator(restClient: RestClient) extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case SignIn(user, password) =>
      restClient.signIn(user, password).onComplete {
        case Success(token: AuthToken) => AuthTokenStore.updateToken(token); sender() ! Authenticated
        case Failure(error)            => sender() ! AuthFailure(error)
      }

    case SignUp(user, password) =>
      restClient.signUp(user, password).onComplete {
        case Success(token: AuthToken) => AuthTokenStore.updateToken(token); sender ! Authenticated
        case Failure(error)            => sender ! AuthFailure(error)
      }

    case SignOut =>
      restClient.signOut().onComplete {
        case Success(ExecutionResultCode.OK) => AuthTokenStore.clear(); sender ! Disconnected
        case Success(_)                      => sender ! AuthFailure(new RuntimeException("Operation failed with unknown error"))
        case Failure(error)                  => sender ! AuthFailure(error)
      }
  }
}

object Authenticator {
  def props(restClient: RestClient): Props = Props(new Authenticator(restClient))
}
