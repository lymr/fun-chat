package rest.client

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.ActorMaterializer
import rest.client.entities.ExecutionResultCode

import scala.concurrent.{ExecutionContext, Future}

trait EntityUnmarshallers {

  implicit val mat: ActorMaterializer
  implicit val ec: ExecutionContext

  def unmarshalEntityTo[A](response: HttpResponse)(implicit u: FromEntityUnmarshaller[A]): Future[A] = {
    response.status match {
      case StatusCodes.OK => Unmarshal(response.entity).to[A]
      case _ =>
        val error = new Exception(response.toString())
        response.discardEntityBytes()
        Future.failed(error)
    }
  }

  def extractStatusCode(response: HttpResponse): Future[Int] = {
    val eventualInt = response.status match {
      case StatusCodes.OK => Future.successful(ExecutionResultCode.OK)
      case _              => Future.failed(new Exception(response.toString()))
    }
    response.discardEntityBytes()
    eventualInt
  }

}
