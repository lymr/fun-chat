package rest.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import authentication.fsm.AuthTokenStore
import authentication.fsm.entities.AuthToken
import com.typesafe.scalalogging.StrictLogging
import rest.client.entities.{ClientInformation, UserInformationEntity}
import rest.client.support.ClientInformationHelper._
import rest.client.support.JsonSupport
import spray.json._
import utils.Configuration

import scala.collection.immutable._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class HttpRestClient(config: Configuration)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer)
    extends RestClient with JsonSupport with StrictLogging {

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val serverUri: Uri =
    Uri().withScheme("http").withHost(config.defaultSeverHost).withPort(config.defaultSeverPort)

  private val originHeader: Origin = Origin(HttpOrigin("http", Host("fun-chat", config.defaultSeverPort)))

  private val clientInformation = ClientInformation(config.clientVersion, ClientIpAddress)

  private val cachedPoolClientFlow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] =
    Http().cachedHostConnectionPool[Int](config.defaultSeverHost, config.defaultSeverPort)

  override def signIn(username: String, password: String): Future[AuthToken] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signIn"))
    val basicAuthHeader          = Authorization(BasicHttpCredentials(username, password))
    val headers: Seq[HttpHeader] = Seq(originHeader, basicAuthHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, clientInformation.toJson.toString)

    Source
      .single(HttpRequest(HttpMethods.POST, requestUri, headers, entity) -> 1)
      .via(cachedPoolClientFlow)
      .via(entityUnmarshallerFlow[AuthToken])
      .runWith(Sink.head)
  }

  override def signUp(username: String, password: String): Future[AuthToken] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signUp"))
    val basicAuthHeader          = Authorization(BasicHttpCredentials(username, password))
    val headers: Seq[HttpHeader] = Seq(originHeader, basicAuthHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, clientInformation.toJson.toString)

    Source
      .single(HttpRequest(HttpMethods.POST, requestUri, headers, entity) -> 2)
      .via(cachedPoolClientFlow)
      .via(entityUnmarshallerFlow[AuthToken])
      .runWith(Sink.head)
  }

  override def signOut(): Future[Int] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signOut"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    Source
      .single(HttpRequest(HttpMethods.POST, requestUri, headers) -> 3)
      .via(cachedPoolClientFlow)
      .via(intUnmarshallerFlow)
      .runWith(Sink.head)
  }

  override def updateCredentials(newPassword: String): Future[Int] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/credentials"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, newPassword)

    Source
      .single(HttpRequest(HttpMethods.PATCH, requestUri, headers, entity) -> 4)
      .via(cachedPoolClientFlow)
      .via(intUnmarshallerFlow)
      .runWith(Sink.head)
  }

  override def listOnlineUsers(): Future[Seq[UserInformationEntity]] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/users"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    Source
      .single(HttpRequest(HttpMethods.GET, requestUri, headers) -> 5)
      .via(cachedPoolClientFlow)
      .via(entityUnmarshallerFlow[Seq[UserInformationEntity]])
      .runWith(Sink.head)
  }

  override def findUserInformation(userName: String): Future[UserInformationEntity] = {
    val requestUri: Uri          = serverUri.withPath(Path(s"/v1/users/name/$userName"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    Source
      .single(HttpRequest(HttpMethods.GET, requestUri, headers) -> 6)
      .via(cachedPoolClientFlow)
      .via(entityUnmarshallerFlow[UserInformationEntity])
      .runWith(Sink.head)
  }

  override def removeUser(): Future[Int] = {
    val requestUri: Uri          = serverUri.withPath(Path(s"/v1/users"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    Source
      .single(HttpRequest(HttpMethods.DELETE, requestUri, headers) -> 7)
      .via(cachedPoolClientFlow)
      .via(intUnmarshallerFlow)
      .runWith(Sink.head)
  }

  override def sendMessage(recipient: String, content: String): Future[Int] = {
    val messagesUri              = serverUri.withPath(Path(s"/v1/messages"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    Source
      .single(HttpRequest(HttpMethods.POST, messagesUri) -> 8)
      .via(cachedPoolClientFlow)
      .via(intUnmarshallerFlow)
      .runWith(Sink.head)
  }

  private def entityUnmarshallerFlow[A](
      implicit unmarshaller: FromEntityUnmarshaller[A]): Flow[(Try[HttpResponse], Int), A, NotUsed] = {

    val extract = (response: HttpResponse) =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[A]
        case _ =>
          val error = new Exception(response.toString())
          response.discardEntityBytes()
          Future.failed(error)
    }

    Flow[(Try[HttpResponse], Int)].mapAsync(1) {
      case (Success(response), _) => extract(response)
      case (Failure(ex), _)       => Future.failed(ex)
    }
  }

  private def intUnmarshallerFlow(): Flow[(Try[HttpResponse], Int), Int, NotUsed] = {

    val extract = (response: HttpResponse) => {
      val eventualInt = response.status match {
        case StatusCodes.OK => Future.successful(0)
        case _              => Future.failed(new Exception(response.toString()))
      }
      response.discardEntityBytes()
      eventualInt
    }

    Flow[(Try[HttpResponse], Int)].mapAsync(1) {
      case (Success(response), _) => extract(response)
      case (Failure(ex), _)       => Future.failed(ex)
    }
  }

}
