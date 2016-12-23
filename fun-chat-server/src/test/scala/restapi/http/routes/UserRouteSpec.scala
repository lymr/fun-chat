package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import base.TestSpec
import core.authentication.tokenGenerators.JwtBearerTokenGenerator
import core.db.users.UsersDao
import core.entities.Defines.AuthToken
import core.entities.{Timer, TokenContext, User}
import org.joda.time.DateTime
import org.mockito.Mock
import restapi.http.JsonSupport
import restapi.http.entities.UserInformationEntity
import restapi.http.routes.UserRouteSpec._
import scalikejdbc.DBSession

import scala.concurrent.Future

class UserRouteSpec extends TestSpec with ScalatestRouteTest with JsonSupport {

  @Mock
  private var mockUsersDao: UsersDao = _

  implicit private var mockApiContext: ApiContext = new ApiContext(
    (token) => Future.successful(BEARER_TOKEN_GENERATOR.decode(token)),
    (name) => userByName(name),
    (_, _) => Unit)

  private var userRoute: UsersRoute = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockUsersDao.findUsers()).thenReturn(USERS)
    when(mockUsersDao.findUserByName(eq(USER_ID_1))(any[DBSession])).thenReturn(Some(USERS.head))
    when(mockUsersDao.findUserByName(eq(USER_ID_2))(any[DBSession])).thenReturn(None)
    userRoute = new UsersRoute(mockUsersDao)
  }

  override def afterAll(): Unit = {
    super.afterAll()

    cleanUp()
  }

  "missing endpoint" in {
    Get("/") ~> userRoute.route ~> check {
      handled shouldBe false
    }
  }

  "check users endpoint" in {
    Get("/users/") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN_1))) ~> userRoute.route ~> check {
      entityAs[Seq[UserInformationEntity]] shouldEqual USERS.map(UserInformationEntity.fromUser)
    }
  }

  "check users endpoint without end slash" in {
    Get("/users") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN_1))) ~> userRoute.route ~> check {
      entityAs[Seq[UserInformationEntity]] shouldEqual USERS.map(UserInformationEntity.fromUser)
    }
  }

  "check post users endpoint" in {
    Post("/users/") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN_1))) ~> userRoute.route ~> check {
      handled shouldBe false
    }
  }

  "check get: users/name/id endpoint" in {
    Get("/users/name/user-id-1") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN_1))) ~> userRoute.route ~> check {
      entityAs[UserInformationEntity] shouldEqual UserInformationEntity.fromUser(USERS.head)
    }
  }

  "check delete: users/name/id endpoint" in {
    Delete("/users/name/user-id-1") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN_1))) ~> userRoute.route ~> check {
      response.status shouldEqual StatusCodes.OK
      verify(mockUsersDao, times(1)).deleteUser(eq(USER_ID_1))(any[DBSession])
    }
  }

  "check try to delete other user-id: users/name/id endpoint" in {
    Delete("/users/name/user-id-1") ~> addHeader(Authorization(OAuth2BearerToken(TOKEN_2))) ~> userRoute.route ~> check {
      response.status shouldEqual StatusCodes.NotAcceptable
      verify(mockUsersDao, times(0)).deleteUser(eq(USER_ID_1))(any[DBSession])
      verify(mockUsersDao, times(0)).deleteUser(eq(USER_ID_2))(any[DBSession])
    }
  }
}

private object UserRouteSpec {
  val USER_ID_1 = "user-id-1"
  val USER_ID_2 = "user-id-2"
  val USER_1    = "user-1"
  val USER_2    = "user-2"

  val USERS = Seq(User(Some(USER_ID_1), USER_1, DateTime.now), User(Some(USER_ID_2), USER_2, DateTime.now))

  val BEARER_TOKEN_GENERATOR = new JwtBearerTokenGenerator(() => "test-secret".toCharArray.map(_.toByte), Timer(180))
  val TOKEN_1: AuthToken     = BEARER_TOKEN_GENERATOR.create(TokenContext(USER_ID_1, USER_1)).get
  val TOKEN_2: AuthToken     = BEARER_TOKEN_GENERATOR.create(TokenContext(USER_ID_2, USER_2)).get

  val userByName: (String) => Option[User] = {
    case USER_ID_1 => Some(User(Some(USER_ID_1), USER_1, DateTime.now))
    case USER_ID_2 => Some(User(Some(USER_ID_2), USER_2, DateTime.now))
  }
}
