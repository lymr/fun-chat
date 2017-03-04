package authentication.fsm

import akka.actor.{ActorRef, FSM}
import authentication.entities._

sealed trait AuthData
case object Uninitialized extends AuthData
case object ProcessingRequest extends AuthData

class AuthenticationFSM(authenticator: ActorRef) extends FSM[AuthState, AuthData] {

  startWith(Offline, Uninitialized)

  when(Offline) {
    case Event(SignIn(username, password), Uninitialized) =>
      authenticator ! SignIn(username, password)
      goto(SigningIn) using ProcessingRequest replying Processing

    case Event(SignUp(username, password), Uninitialized) =>
      authenticator ! SignUp(username, password)
      goto(SigningIn) using ProcessingRequest replying Processing
  }

  when(Online) {
    case Event(SignOut, Uninitialized) =>
      authenticator ! SignOut
      goto(SigningOut) using ProcessingRequest replying Processing
  }

  when(SigningIn) {
    case Event(ProcessingDone(result), ProcessingRequest) =>
      result match {
        case Success => goto(Online) using Uninitialized replying Success
        case Failure => goto(Offline) using Uninitialized replying Failure
      }
  }

  when(SigningOut) {
    case Event(ProcessingDone(result), ProcessingRequest) =>
      result match {
        case Success => goto(Offline) using Uninitialized replying Success
        case Failure => goto(Online) using Uninitialized replying Failure
      }
  }

  onTransition {
    case Offline -> SigningIn  => log.info("Client signing-in...")
    case SigningIn -> Online   => log.info("Client online.")
    case Online -> SigningOut  => log.info("Client signing-out...")
    case SigningOut -> Offline => log.info("Client offline.")
  }

  initialize()
}
