package messages

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import messages.entities.TextMessage

class Messenger(implicit materializer: ActorMaterializer) extends Actor {

  override def receive: Receive = {
    case msg: TextMessage => deliver(msg)
  }

  def deliver(msg: TextMessage): Unit = {
    val httpHandler = Http(context.system)
    val requestUri  = Uri().withScheme("http").withHost(msg.recipientInfo.ipAddress).withPort(8080)
    val httpRequest = HttpRequest().withMethod(HttpMethods.POST).withUri(requestUri).withEntity(msg.content)
    httpHandler.singleRequest(httpRequest)
  }
}

object Messenger {
  def props()(implicit materializer: ActorMaterializer): Props = Props(new Messenger())
}
