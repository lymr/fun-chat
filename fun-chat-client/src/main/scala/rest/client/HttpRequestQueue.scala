package rest.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}
import rest.client.HttpRequestQueue._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

trait HttpRequestQueue {

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer
  implicit val ec: ExecutionContext

  val QueueSize: Int

  val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]),
                           (Try[HttpResponse], Promise[HttpResponse]),
                           Http.HostConnectionPool]

  private val queue =
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.backpressure)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case ((Success(httpResponse), promisedResponse)) => promisedResponse.success(httpResponse)
        case ((Failure(throwable), promisedResponse))    => promisedResponse.failure(throwable)
      }))(Keep.left)
      .run()

  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue
      .offer(request -> responsePromise)
      .map {
        case QueueOfferResult.Enqueued    => responsePromise
        case QueueOfferResult.Dropped     => responsePromise.failure(new RuntimeException(QUEUE_OVERFLOW))
        case QueueOfferResult.QueueClosed => responsePromise.failure(new RuntimeException(QUEUE_CLOSED))
        case QueueOfferResult.Failure(ex) => responsePromise.failure(ex)
      }
      .flatMap(_.future)
  }
}

object HttpRequestQueue {
  private val QUEUE_OVERFLOW: String = "Queue overflowed. Try again later."
  private val QUEUE_CLOSED: String   = "Queue was closed (pool shut down) while running the request. Try again later."
}
