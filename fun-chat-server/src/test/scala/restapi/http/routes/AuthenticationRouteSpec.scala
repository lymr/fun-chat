package restapi.http.routes

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import base.TestSpec
import core.authentication.AuthenticationService
import core.entities.Defines.AuthToken
import org.mockito.Mock
import restapi.http.JsonSupport
import restapi.http.entities.ClientInformation
import restapi.http.routes.AuthenticationRouteSpec._
import spray.json._

import scala.concurrent.Future

class AuthenticationRouteSpec extends TestSpec with ScalatestRouteTest with JsonSupport {

  @Mock
  private var mockAuthService: AuthenticationService = _

  implicit private var mockApiContext: ApiContext =
    new ApiContext((_: AuthToken) => Future.successful(None), (_: String) => None)

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

  "sign in with basic credentials, with client information, request pass" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signIn", entity) ~> addHeader(Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> authRoute.route ~> check {
      verify(mockAuthService, times(1)).signIn(USERNAME, PASSWORD, CLIENT_INFO)
    }
  }

  "sign in with no credentials, request is unauthorized" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signIn", entity) ~> authRoute.route ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
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
      verify(mockAuthService, times(1)).signUp(USERNAME, PASSWORD, CLIENT_INFO)
    }
  }

  "sign up with no credentials, request is unauthorized" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/auth/signUp", entity) ~> authRoute.route ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
    }
  }

  //TODO: add more tests to delete and update credentials
}

private object AuthenticationRouteSpec {
  val USERNAME = "username-1"
  val PASSWORD = "p@ssword"

  val CLIENT_INFO = ClientInformation("v1.0", "10.1.1.138")
}
