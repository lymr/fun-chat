package messages

import akka.actor.{Actor, Props}
import messages.entities.ProcessedMessage

class Messenger() extends Actor {

  override def receive: Receive = {
    case message: ProcessedMessage => ???
  }
}

object Messenger {
  def props(): Props = Props(new Messenger())
}
