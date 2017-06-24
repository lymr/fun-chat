package rest.client

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.stream.ActorMaterializer
import authentication.AuthTokenStore
import authentication.entities.AuthToken
import com.typesafe.scalalogging.StrictLogging
import rest.client.entities.{ClientInformation, UserInformationEntity}
import rest.client.support.ClientInformationHelper._
import rest.client.support.JsonSupport
import spray.json._
import utils.Configuration

import scala.collection.immutable._
import scala.concurrent.{ExecutionContext, Future, Promise}

//TODO: Send / Receive message should be done using a socket.
class HttpRestClient(config: Configuration)(implicit val system: ActorSystem, val mat: ActorMaterializer)
  extends RestClient
    with HttpRequestQueue
    with EntityUnmarshallers
    with JsonSupport
    with StrictLogging {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  override val QueueSize: Int = config.restClientQueueSize
  override val poolClientFlow =
    Http().cachedHostConnectionPool[Promise[HttpResponse]](config.defaultSeverHost, config.defaultSeverPort)

  private val serverUri: Uri =
    Uri().withScheme("http").withHost(config.defaultSeverHost).withPort(config.defaultSeverPort)

  private val clientInformation = ClientInformation(config.clientVersion, ClientIpAddress)

  private val originHeader: Origin = Origin(HttpOrigin("http", Host("fun-chat", config.defaultSeverPort)))

  override def signIn(username: String, password: String): Future[AuthToken] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signIn"))
    val basicAuthHeader          = Authorization(BasicHttpCredentials(username, password))
    val headers: Seq[HttpHeader] = Seq(originHeader, basicAuthHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, clientInformation.toJson.toString)

    queueRequest(HttpRequest(HttpMethods.POST, requestUri, headers, entity))
      .flatMap(unmarshalEntityTo[AuthToken])
  }

  override def signUp(username: String, password: String): Future[AuthToken] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signUp"))
    val basicAuthHeader          = Authorization(BasicHttpCredentials(username, password))
    val headers: Seq[HttpHeader] = Seq(originHeader, basicAuthHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, clientInformation.toJson.toString)

    queueRequest(HttpRequest(HttpMethods.POST, requestUri, headers, entity))
      .flatMap(unmarshalEntityTo[AuthToken])
  }

  override def signOut(): Future[Int] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/signOut"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    queueRequest(HttpRequest(HttpMethods.POST, requestUri, headers))
      .flatMap(extractStatusCode)
  }

  override def updateCredentials(newPassword: String): Future[Int] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/auth/credentials"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)
    val entity: RequestEntity    = HttpEntity(ContentTypes.`application/json`, newPassword)

    queueRequest(HttpRequest(HttpMethods.PATCH, requestUri, headers, entity))
      .flatMap(extractStatusCode)
  }

  override def listOnlineUsers(): Future[Seq[UserInformationEntity]] = {
    val requestUri: Uri          = serverUri.withPath(Path("/v1/users"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    queueRequest(HttpRequest(HttpMethods.GET, requestUri, headers))
      .flatMap(unmarshalEntityTo[Seq[UserInformationEntity]])
  }

  override def findUserInformation(userName: String): Future[UserInformationEntity] = {
    val requestUri: Uri          = serverUri.withPath(Path(s"/v1/users/name/$userName"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    queueRequest(HttpRequest(HttpMethods.GET, requestUri, headers))
      .flatMap(unmarshalEntityTo[UserInformationEntity])
  }

  override def removeUser(): Future[Int] = {
    val requestUri: Uri          = serverUri.withPath(Path(s"/v1/users"))
    val bearerTokenHeader        = Authorization(OAuth2BearerToken(AuthTokenStore.getBearerToken))
    val headers: Seq[HttpHeader] = Seq(originHeader, bearerTokenHeader)

    queueRequest(HttpRequest(HttpMethods.DELETE, requestUri, headers))
      .flatMap(extractStatusCode)
  }
}
