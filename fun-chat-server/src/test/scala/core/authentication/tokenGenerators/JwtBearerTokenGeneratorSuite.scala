package core.authentication.tokenGenerators

import core.authentication.tokenGenerators.JwtBearerTokenGeneratorSuite._
import core.entities._
import tests.TestSuite

class JwtBearerTokenGeneratorSuite extends TestSuite {

  var tokenGenerator: (Timer) => JwtBearerTokenGenerator = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    tokenGenerator = (timer: Timer) => new JwtBearerTokenGenerator(() => SECRET_KEY, timer)
  }

  test("creating token, token is defined") {
    val claims = AuthTokenClaims(USER_ID, USER_NAME, SESSION_ID)

    val token = tokenGenerator(TIMER).create(claims)

    assert(token.isDefined)
  }

  test("decoding token, context is returned correctly") {
    val generator = tokenGenerator(TIMER)
    val claims    = AuthTokenClaims(USER_ID, USER_NAME, SESSION_ID)
    val token     = generator.create(claims)

    val result = generator.decode(token.get)

    assert(result.isDefined)
    assertResult(claims)(result.get)
  }

  test("token issue date is before current time minus expiration interval, token invalid") {
    val testableTimer = new TestableTimer(System.currentTimeMillis - EXPIRATION * 3 * 1000, EXPIRATION)
    val generator = tokenGenerator(testableTimer)
    val claims    = AuthTokenClaims(USER_ID, USER_NAME, SESSION_ID)
    val token     = generator.create(claims)

    val result = generator.decode(token.get)

    assert(result.isEmpty)
  }

  test("token expiration date is after current time plus expiration interval, token invalid") {
    val testableTimer = new TestableTimer(System.currentTimeMillis + EXPIRATION * 3 * 1000, EXPIRATION)
    val generator = tokenGenerator(testableTimer)
    val claims    = AuthTokenClaims(USER_ID, USER_NAME, SESSION_ID)
    val token     = generator.create(claims)

    val result = generator.decode(token.get)

    assert(result.isEmpty)
  }

  class TestableTimer(timestamp: Long, intervalStep: Long) extends Timer(intervalStep) {
    override def freeze: IntervalTimestamp = IntervalTimestamp(timestamp, intervalStep)
  }
}

object JwtBearerTokenGeneratorSuite {
  private val USER_ID: UserID          = UserID("user-id-xx")
  private val USER_NAME: String        = "test-user"
  private val SESSION_ID: SessionID    = SessionID("test-session-id")
  private val EXPIRATION: Long         = 120
  private val TIMER: Timer             = Timer(EXPIRATION)
  private val SECRET_KEY: SecuredToken = SecuredToken("^fun-ch@t-test-sec7et-k3y!".toCharArray.map(_.toByte))
}
