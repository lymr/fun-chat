package core.authentication.tokenGenerators

import base.TestSuite
import core.authentication.tokenGenerators.JwtBearerTokenGeneratorSuite._
import core.entities.TokenContext

class JwtBearerTokenGeneratorSuite extends TestSuite {

  var tokenGenerator: JwtBearerTokenGenerator = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    tokenGenerator = new JwtBearerTokenGenerator(() => SecretKey, TokenExpirationInterval)
  }

  test("creating token, token is defined") {
    val ctx = TokenContext("user-id-xx", "test-user")

    val token = tokenGenerator.create(ctx)

    assert(token.isDefined)
  }

  test("decoding token, context is returned") {
    val ctx   = TokenContext("user-id-xx", "test-user")
    val token = tokenGenerator.create(ctx)

    val result = tokenGenerator.decode(token.get)

    assert(result.isDefined)
    assertResult(ctx)(result.get)
  }

  test("verifying token, is OK") {
    val ctx   = TokenContext("user-id-xx", "test-user")
    val token = tokenGenerator.create(ctx)

    val result = tokenGenerator.isValid(token.get)

    assert(result)
  }
}

object JwtBearerTokenGeneratorSuite {
  val SecretKey: Array[Byte]  = "^fun-ch@t-test-sec7et-k3y!".toCharArray.map(_.toByte)
  val TokenExpirationInterval = 1000L
}
