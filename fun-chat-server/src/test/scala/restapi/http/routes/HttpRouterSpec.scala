package restapi.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{InvalidOriginRejection, MissingHeaderRejection}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import base.TestSpec
import core.authentication.AuthenticationService
import core.db.DatabaseContext
import core.db.clients.ConnectedClientsStore
import org.mockito.Mock
import restapi.http.JsonSupport
import restapi.http.entities.ClientInformation
import restapi.http.routes.HttpRouterSpec._
import restapi.http.routes.support.AllowedOrigins._
import spray.json._

class HttpRouterSpec extends TestSpec with ScalatestRouteTest with JsonSupport {

  val probe: TestProbe    = TestProbe()
  val mockActor: ActorRef = probe.ref

  @Mock
  private var dbContext: DatabaseContext = _

  @Mock
  private var authService: AuthenticationService = _

  @Mock
  private var connectedClientsStore: ConnectedClientsStore = _

  private var httpRouter: HttpRouter = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    httpRouter = new HttpRouter(dbContext, authService, connectedClientsStore, mockActor)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    cleanUp()
  }

  "cors support check, accepted" in {
    Options("/v1") ~> addHeader(Origin(HttpOrigin("http", Host("fun-chat", 8080)))) ~> httpRouter.routes ~> check {
      response shouldEqual HttpResponse(StatusCodes.OK).withHeaders(
        `Access-Control-Allow-Origin`(HttpOriginRange.Default(allowedOrigins)),
        `Access-Control-Allow-Credentials`(allow = true),
        `Access-Control-Allow-Methods`(GET, POST, PATCH, DELETE),
        `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With"))
    }
  }

  "cors support check, rejected" in {
    Options("/v1") ~> addHeader(Origin(HttpOrigin("http", Host("fun*chat", 8080)))) ~> httpRouter.routes ~> check {
      rejection shouldEqual InvalidOriginRejection(allowedOrigins)
    }
  }

  "authentication route check without origin header, request rejected" in {
    Post("/v1/auth/signIn") ~> httpRouter.routes ~> check {
      rejection shouldEqual MissingHeaderRejection("Origin")
    }
  }

  "authentication route check with origin header, request accepted" in {
    val entity = HttpEntity(ContentTypes.`application/json`, CLIENT_INFO.toJson.toString)
    Post("/v1/auth/signIn", entity) ~> addHeader(Origin(HttpOrigin("http", Host("fun-chat", 8080)))) ~> addHeader(
      Authorization(BasicHttpCredentials(USERNAME, PASSWORD))) ~> httpRouter.routes ~> check {
      handled shouldBe true
    }
  }

  "user route check" in {
    Get("/v1/users") ~> addHeader(Origin(HttpOrigin("http", Host("fun-chat", 8080)))) ~> httpRouter.routes ~> check {
      handled shouldBe true
    }
  }

  "messages route check" in {
    Post("/v1/messages/") ~> addHeader(Origin(HttpOrigin("http", Host("fun-chat", 8080)))) ~> httpRouter.routes ~> check {
      handled shouldBe true
    }
  }

  "messages route recipient endpoint check" in {
    Post("/v1/messages/recipient/some-name") ~> addHeader(Origin(HttpOrigin("http", Host("fun-chat", 8080)))) ~> httpRouter.routes ~> check {
      handled shouldBe true
    }
  }
}

object HttpRouterSpec {
  val USERNAME    = "username-1"
  val PASSWORD    = "p@ssword"
  val CLIENT_INFO = ClientInformation("v1.0", "10.1.1.138")
}
