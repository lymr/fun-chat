package restapi.http.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import base.TestWordSpec
import core.authentication.AuthenticationService
import core.authentication.tokenGenerators.JwtBearerTokenGenerator
import core.entities._
import org.joda.time.DateTime
import org.mockito.Mock
import restapi.http.JsonSupport
import restapi.http.entities.ClientInformation
import restapi.http.routes.AuthenticationRouteSpec._
import spray.json._

import scala.concurrent.Future

class AuthenticationRouteSpec extends TestWordSpec with ScalatestRouteTest with JsonSupport {

  @Mock
  private var mockAuthService: AuthenticationService = _

  implicit private var mockApiContext: ApiContext = new ApiContext(authenticate, userByName)

  private var authRoute: AuthenticationRoute = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    authRoute = new AuthenticationRoute(mockAuthService)
  }

  override def afterAll(): Unit = {
    super.afterAll()

    cleanUp()
  }

  "call to auth without leading path, request is not accepted" in {
    Post("/auth/") ~> authRoute.route ~> check {
      handled shouldBe false
    }
  }

  "sign in with basic credentials, without client information" in {
    Post("/auth/signIn") ~> addHeader(Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> authRoute.route ~> check {
      rejection
    }
  }

  "sign in with wrong method, request rejected" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Get("/auth/signIn", entity) ~> addHeader(Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> authRoute.route ~> check {
      rejection shouldEqual MethodRejection(HttpMethods.POST)
    }
  }

  "sign in with basic credentials, with client information, request pass" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signIn", entity) ~> addHeader(Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> authRoute.route ~> check {
      verify(mockAuthService, times(1)).signIn(USERNAME, SECRET, CLIENT_INFO)
    }
  }

  "sign in with no credentials, request is unauthorized" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signIn", entity) ~> authRoute.route ~> check {
      response withStatus StatusCodes.Unauthorized
    }
  }

  "sign up with basic credentials, without client information" in {
    Post("/auth/signUp") ~> addHeader(Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> authRoute.route ~> check {
      rejection
    }
  }

  "sign up with basic credentials, with client information, request pass" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signUp", entity) ~> addHeader(Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> authRoute.route ~> check {
      verify(mockAuthService, times(1)).signUp(USERNAME, SECRET, CLIENT_INFO)
    }
  }

  "sign up with no credentials, request is unauthorized" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signUp", entity) ~> authRoute.route ~> check {
      response withStatus StatusCodes.Unauthorized
    }
  }

  "sign-out user with no token, request is rejected" in {
    Post("/auth/signOut") ~> authRoute.route ~> check {
      response withStatus StatusCodes.Unauthorized
    }
  }

  "sign-out with token, user is signed-out" in {
    Post("/auth/signOut") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN))) ~> authRoute.route ~> check {
      verify(mockAuthService).signOut(eq(USER_ID))
    }
  }

  "credentials request without token, request unauthorized" in {
    Patch("/auth/credentials") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN))) ~> authRoute.route ~> check {
      handled shouldBe false
    }
  }

  "credentials request with wrong method, request is not handled" in {
    Post("/credentials") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN))) ~> authRoute.route ~> check {
      rejection shouldEqual MethodRejection(HttpMethods.PATCH)
    }
  }

  "update credentials with no token, request is rejected" in {
    Patch("/credentials") ~> authRoute.route ~> check {
      response withStatus StatusCodes.Unauthorized
    }
  }

  "update credentials with valid token, request is processed" in {
    val entity = HttpEntity(ContentTypes.`application/json`, NEW_PASSWORD)
    Patch("/credentials", entity) ~> addHeader(Authorization(OAuth2BearerToken(TOKEN))) ~> authRoute.route ~> check {
      verify(mockAuthService, times(1)).updateCredentials(eq(USER_ID), eq(NEW_SECRET))
    }
  }
}

private object AuthenticationRouteSpec {
  val USER_ID      = UserID("user-id-1")
  val USERNAME     = "username-1"
  val PASSWORD     = "p@ssword"
  val NEW_PASSWORD = "p@sswo7d"
  val SECRET       = UserSecret(PASSWORD)
  val NEW_SECRET   = UserSecret(NEW_PASSWORD)

  val CLIENT_INFO   = ClientInformation("v1.0", "10.1.1.138")
  val SECURED_TOKEN = SecuredToken("test-secret".toCharArray.map(_.toByte))

  val BEARER_TOKEN_GENERATOR =
    new JwtBearerTokenGenerator(() => SECURED_TOKEN, Timer(180))
  val TOKEN: String = BEARER_TOKEN_GENERATOR.create(AuthTokenContext(USER_ID, USERNAME)).get.token

  val userByName: String => Option[User] = {
    case USERNAME => Some(User(USER_ID, USERNAME, DateTime.now))
    case _        => None
  }

  val authenticate: AuthToken => Future[Option[AuthTokenContext]] = {
    case (bearer: BearerToken) => Future.successful(BEARER_TOKEN_GENERATOR.decode(bearer))
    case _                     => Future.failed(new IllegalArgumentException("Unsupported AuthToken."))
  }
}
