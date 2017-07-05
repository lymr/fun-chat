package core.db.users

import core.authentication.UserSecretUtils
import core.authentication.tokenGenerators.SecuredTokenGenerator
import core.db.users.SqlUserDaoSuite._
import core.entities.{CredentialSet, SecuredToken, UserID, UserSecret}
import org.scalatest.Ignore
import tests.FixtureTestSuite

@Ignore
class SqlUserDaoSuite extends FixtureTestSuite {

  private var credentialsDao: UserCredentialsDao = _

  private var usersDao: UsersDao = _

  private var securedToken: SecuredToken = _

  private var credentialsGenerator: UserSecret => Option[CredentialSet] = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    securedToken = SecuredTokenGenerator.generate()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    usersDao.findUsers().foreach(u => usersDao.deleteUser(u.userId))
  }

  test("create new user") { implicit session =>
    initialize()
    val user = usersDao.createUser(USER, SECRET)

    val credentials = credentialsDao.findUserCredentials(user.userId)

    val expected = UserSecretUtils.encrypt(SECRET, () => securedToken)
    assertResult(expected)(credentials)
  }

  test("update user") { implicit session =>
    initialize()
    val user = usersDao.createUser(USER, SECRET)
    usersDao.updateUser(user.userId, NEW_SECRET)

    val credentials = credentialsDao.findUserCredentials(user.userId)

    val expected = UserSecretUtils.encrypt(NEW_SECRET, () => securedToken)
    assertResult(expected)(credentials)
  }

  test("credentials dao throws exception on create, transaction is aborted and db not affected") { implicit session =>
    initialize(() => {
      throw new RuntimeException()
      securedToken
    })

    intercept[RuntimeException] {
      usersDao.createUser(USER, SECRET)
    }

    val users = usersDao.findUsers()
    assert(users.isEmpty)
  }

  test("credentials dao throws exception on update, transaction is aborted and db not affected") { implicit session =>
    initialize()
    val user = usersDao.createUser(USER, SECRET)

    initialize(() => {
      throw new RuntimeException()
      securedToken
    })

    intercept[RuntimeException] {
      usersDao.updateUser(user.userId, NEW_SECRET)
    }

    val credentials = credentialsDao.findUserCredentials(user.userId)

    val expected = UserSecretUtils.encrypt(SECRET, () => securedToken)
    assertResult(expected)(credentials)
  }

  test("add three users, find all returns all of them") { implicit session =>
    initialize()
    val users = Set(usersDao.createUser(USER + "1", SECRET),
                    usersDao.createUser(USER + "2", SECRET),
                    usersDao.createUser(USER + "3", SECRET))

    val result = usersDao.findUsers().toSet

    assertResult(users)(result)
  }

  test("create two users with different name, search by name returns matching user") { implicit session =>
    initialize()
    val user1 = usersDao.createUser(USER + "1", SECRET)
    val user2 = usersDao.createUser(USER + "2", SECRET)

    val result = usersDao.findUserByName(USER + "1")

    assert(result.isDefined)
    assertResult(user1)(result.get)
  }

  test("create and delete user, final all is an empty set") { implicit session =>
    initialize()
    val user = usersDao.createUser(USER, SECRET)
    usersDao.deleteUser(user.userId)

    val maybeUser = usersDao.findUserByName(USER)
    val users     = usersDao.findUsers()

    assert(maybeUser.isEmpty)
    assert(users.isEmpty)
  }

  private def initialize(generator: () => SecuredToken = () => securedToken): Unit = {
    credentialsGenerator = UserSecretUtils.encrypt(_, generator)
    credentialsDao = new SqlUserCredentialsDao(credentialsGenerator)
    usersDao = new SqlUsersDao(credentialsDao)
  }

}

object SqlUserDaoSuite {
  private val USER_ID: UserID        = UserID("some-id-x-x-x")
  private val USER: String           = "test-user"
  private val SECRET: UserSecret     = UserSecret("s3c7et")
  private val NEW_SECRET: UserSecret = UserSecret("n3w-s3c7et")
}
