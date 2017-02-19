package rest.client

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import rest.client.entities.{ClientInformation, UserInformationEntity}
import rest.client.support.JsonSupport
import spray.json._
import utils.Configuration

import scala.collection.immutable._
import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class HttpRestClient(config: Configuration)
                    (implicit actorSystem: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext)
    extends RestClient with JsonSupport with StrictLogging {

  private var authToken: Option[String] = None

  private val serverUri: Uri =
    Uri().withScheme("http").withHost(config.defaultSeverHost).withPort(config.defaultSeverPort)

  private val originHeader: Origin = Origin(HttpOrigin("http", Host("fun-chat", config.defaultSeverPort)))

  private val clientInformation = ClientInformation(config.clientVersion, getClientIpAddress())

  private val poolClientFlow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] =
    Http().cachedHostConnectionPool[Int](config.defaultSeverHost, config.defaultSeverPort)

  override def signIn(username: String, password: String): Unit = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signIn"))
    val basicAuthHeader          = Authorization(BasicHttpCredentials(username, password))
    val headers: Seq[HttpHeader] = Seq(originHeader, basicAuthHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, clientInformation.toJson.toString)

    val responseFuture: Future[(Try[HttpResponse], Int)] =
      Source
        .single(HttpRequest(HttpMethods.POST, requestUri, headers, entity) -> 1)
        .via(poolClientFlow)
        .runWith(Sink.head)

    responseFuture.onComplete {
      case Success((triedResponse, _)) =>
        triedResponse.toOption.foreach {
          case HttpResponse(statusCode, _, `entity`, _) if statusCode == StatusCodes.OK => extractAuthToken(entity)
        }
      case Failure(ex) => logger.error("Failed to signIn, error = ", ex); false
    }
  }

  override def signUp(username: String, password: String): Unit = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signUp"))
    val basicAuthHeader          = Authorization(BasicHttpCredentials(username, password))
    val headers: Seq[HttpHeader] = Seq(originHeader, basicAuthHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, clientInformation.toJson.toString)

    val responseFuture: Future[(Try[HttpResponse], Int)] =
      Source
        .single(HttpRequest(HttpMethods.POST, requestUri, headers, entity) -> 2)
        .via(poolClientFlow)
        .runWith(Sink.head)

    responseFuture.onComplete {
      case Success((triedResponse, _)) =>
        triedResponse.toOption.foreach {
          case HttpResponse(statusCode, _, `entity`, _) if statusCode == StatusCodes.OK => extractAuthToken(entity)
        }
      case Failure(ex) => logger.error("Failed to signIn, error = ", ex); false
    }
  }

  override def signOut(): Unit = {
    require(authToken.isDefined, "Client is signed-out")

    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signOut"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(authToken.get))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    val responseFuture: Future[(Try[HttpResponse], Int)] =
      Source
        .single(HttpRequest(HttpMethods.POST, requestUri, headers) -> 3)
        .via(poolClientFlow)
        .runWith(Sink.head)

    responseFuture.onComplete {
      case Success((triedResponse, _)) =>
        triedResponse.toOption.foreach {
          case HttpResponse(statusCode, _, _, _) if statusCode == StatusCodes.OK => logger.info("Client signed-out!")
        }
      case Failure(ex) => logger.error("Failed to signIn, error = ", ex); false
    }
  }

  override def listOnlineUsers(): Seq[UserInformationEntity] = ???

  override def sendMessage(recipient: String, content: String): Unit = {
    val messagesUri = serverUri.withPath(Path(s"/v1/messages/recipient/$recipient"))
    val responseFuture: Future[(Try[HttpResponse], Int)] =
      Source
        .single(HttpRequest(HttpMethods.POST, messagesUri) -> 42)
        .via(poolClientFlow)
        .runWith(Sink.head)

    responseFuture.andThen {
      case Success(_)  =>
      case Failure(ex) =>
    }
  }

  override def options(): Unit = {}

  private def extractAuthToken(entity: ResponseEntity): Unit = {
    val strictEntity: Future[HttpEntity.Strict] = entity.toStrict(3.seconds)

    val transformedData: Future[String] =
      strictEntity flatMap { e =>
        e.dataBytes
          .runFold(ByteString.empty)(_ ++ _)
          .map(x => x.utf8String)
      }

    transformedData.onComplete {
      case Success(token) => authToken = Some(token)
      case Failure(ex)    => logger.error("Failed to extract authentication token, error = ", ex)
    }
  }

  private def getClientIpAddress(): String = {
    val localhost = InetAddress.getLocalHost
    localhost.getHostAddress
  }
}
