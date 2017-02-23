package messages

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import messages.entities.ProcessedTextMessage

class Messenger(implicit materializer: ActorMaterializer) extends Actor {

  override def receive: Receive = {
    case msg: ProcessedTextMessage => deliver(msg)
  }

  def deliver(msg: ProcessedTextMessage): Unit = {
    val httpHandler = Http(context.system)
    val requestUri  = Uri().withScheme("http").withHost(msg.recipientClientInfo.ipAddress).withPort(8080)
    val httpRequest = HttpRequest().withMethod(HttpMethods.POST).withUri(requestUri).withEntity(msg.content)
    httpHandler.singleRequest(httpRequest)
  }
}

object Messenger {
  def props()(implicit materializer: ActorMaterializer): Props = Props(new Messenger())
}
