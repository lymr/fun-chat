package commands.fsm

import akka.actor.{Actor, FSM}
import rest.client.RestClient

sealed trait Command
case object Done    extends Command
case object Help    extends Command
case object Exit    extends Command
case object SignOut extends Command
final case class SignIn(username: String, password: String) extends Command
final case class SignUp(username: String, password: String) extends Command

sealed trait ClientState
case object Offline extends ClientState
case object Online  extends ClientState

final case class ClientInformation(isOnline: Boolean)

class ClientFSM(restClient: RestClient) extends Actor with FSM[ClientState, ClientInformation] {

  startWith(Offline, ClientInformation(isOnline = false))

  when(Offline) {
    case Event(Exit, _) => stop()

    case Event(SignIn(username, password), _) =>
      val result = restClient.signIn(username, password)
      goto(Online) using ClientInformation(isOnline = true) replying Done

    case Event(SignUp(username, password), _) =>
      val result = restClient.signUp(username, password)
      goto(Online) using ClientInformation(isOnline = true) replying Done
  }

  when(Online) {
    case Event(SignOut, _) =>
      val result = restClient.signOut()
      goto(Offline) using ClientInformation(isOnline = false) replying Done

    case Event(Exit, _) =>
      val result = restClient.signOut()
      stop()
  }

  initialize()

}
