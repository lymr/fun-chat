package restapi.http.routes

import akka.http.scaladsl.testkit.ScalatestRouteTest
import base.TestSuite
import core.db.users.UsersDao
import org.mockito.Mock

class UserRouteSuite extends TestSuite with ScalatestRouteTest {

  @Mock
  private var mockUsersDao: UsersDao = _

  @Mock
  implicit private var mockApiContext: ApiContext = _

  private var userRoute: UsersRoute = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    userRoute = new UsersRoute(mockUsersDao)
  }

  test("missing endpoint") {
    Get("/") ~> userRoute.route ~> check {
      !handled
    }
  }

  test("check users endpoint") {
    Get("users/") ~> userRoute.route ~> check {
      handled
    }
  }
}
