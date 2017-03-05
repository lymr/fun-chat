package authentication

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import authentication.entities.{AuthToken, _}
import rest.client.RestClient
import rest.client.entities.ExecutionResultCode

import scala.concurrent.ExecutionContext

class Authenticator(restClient: RestClient) extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case SignIn(user, password) =>
      restClient.signIn(user, password)
        .recover {
          case ex: Throwable => AuthFailure(ex)
        }
        .map {
          case (token: AuthToken) => AuthTokenStore.updateToken(token); Authenticated
        } pipeTo sender()

    case SignUp(user, password) =>
      restClient.signUp(user, password)
        .recover {
          case ex: Throwable => AuthFailure(ex)
        }
        .map {
          case (token: AuthToken) => AuthTokenStore.updateToken(token); Authenticated
        } pipeTo sender()

    case SignOut =>
      restClient.signOut()
        .recover {
          case ex: Throwable => AuthFailure(ex)
        }
        .map {
          case ExecutionResultCode.OK => AuthTokenStore.clear(); Disconnected
          case _                      => AuthFailure(new RuntimeException("Operation failed with unknown error"))
        } pipeTo sender()
  }
}

object Authenticator {
  def props(restClient: RestClient): Props = Props(new Authenticator(restClient))
}
