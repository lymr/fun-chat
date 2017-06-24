package authentication.fsm

import akka.actor.{ActorRef, FSM, Props}
import authentication.entities._
import authentication.fsm.AuthenticationFSM._

class AuthenticationFSM(authenticator: ActorRef) extends FSM[AuthState, StateData] {

  startWith(Offline, Uninitialized)

  when(Offline) {
    case Event(SignIn(username, password), Uninitialized) =>
      authenticator ! SignIn(username, password)
      goto(SigningIn) using RequestInfo(sender)

    case Event(SignUp(username, password), Uninitialized) =>
      authenticator ! SignUp(username, password)
      goto(SigningIn) using RequestInfo(sender)
  }

  when(Online) {
    case Event(SignOut, Uninitialized) =>
      authenticator ! SignOut
      goto(SigningOut) using RequestInfo(sender)

    case Event(UpdateCredentials(password), Uninitialized) =>
      authenticator ! UpdateCredentials(password)
      goto(Updating) using RequestInfo(sender)
  }

  when(SigningIn) {
    case Event(Authenticated, RequestInfo(initiator)) =>
      initiator ! Authenticated
      goto(Online) using Uninitialized

    case Event(AuthFailure(error), RequestInfo(initiator)) =>
      initiator ! AuthFailure(error)
      goto(Offline) using Uninitialized
  }

  when(SigningOut) {
    case Event(Disconnected, RequestInfo(initiator)) =>
      initiator ! Disconnected
      goto(Offline) using Uninitialized

    case Event(AuthFailure(error), RequestInfo(initiator)) =>
      initiator ! AuthFailure(error)
      goto(Online) using Uninitialized
  }

  when(Updating) {
    case Event(_, _) => //TODO: implement!
      stay()
  }

  onTransition {
    case Offline -> SigningIn  => log.info("Client signing-in...")
    case SigningIn -> Online   => log.info("Client online.")
    case Online -> SigningOut  => log.info("Client signing-out...")
    case SigningOut -> Offline => log.info("Client offline.")
  }

  initialize()
}

object AuthenticationFSM {
  def props(authenticator: ActorRef): Props = Props(new AuthenticationFSM(authenticator))

  sealed trait StateData
  private case object Uninitialized extends StateData
  private case class RequestInfo(initiator: ActorRef) extends StateData
}
