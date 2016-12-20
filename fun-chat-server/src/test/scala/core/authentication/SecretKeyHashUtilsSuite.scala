package core.authentication

import base.TestSuite
import core.authentication.SecretKeyHashUtilsSuite._
import core.entities.CredentialSet

class SecretKeyHashUtilsSuite extends TestSuite {

  test("encryption of same password with same slat results with same hash") {
    val expected = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toCharArray.map(_.toByte))
    val result   = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toCharArray.map(_.toByte))

    assert(expected.isDefined)
    assert(result.isDefined)
    assertResult(expected.get.password)(result.get.password)
    assertResult(expected.get.salt)(result.get.salt)
  }

  test("encryption of non same password with same slat results are not matching") {
    val expected = SecretKeyHashUtils.generate(TestPassword.toUpperCase.toCharArray, TestSalt.toCharArray.map(_.toByte))
    val result = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toCharArray.map(_.toByte))

    assert(expected.isDefined)
    assert(result.isDefined)
    assert(!expected.get.password.equals(result.get.password))
    assert(!expected.get.salt.equals(result.get.salt))
  }

  test("encryption of same password with non same slat results are not matching") {
    val expected = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toCharArray.map(_.toByte))
    val result   = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toUpperCase.toCharArray.map(_.toByte))

    assert(expected.isDefined)
    assert(result.isDefined)
    assert(!expected.get.password.equals(result.get.password))
    assert(!expected.get.salt.equals(result.get.salt))
  }

  test("verifying of same password with same slat results with same hash") {
    val expected = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toCharArray.map(_.toByte))
    val credentials = CredentialSet(expected.get.password, TestSalt.toCharArray.map(_.toByte), "")

    val result = SecretKeyHashUtils.validate(credentials, TestPassword)

    assert(result)
  }

  test("verifying of non same password with same slat results with same hash") {
    val expected = SecretKeyHashUtils.generate(TestPassword.toCharArray, TestSalt.toCharArray.map(_.toByte))
    val credentials = CredentialSet(expected.get.password, TestSalt.toCharArray.map(_.toByte), "")

    val result = SecretKeyHashUtils.validate(credentials, TestPassword.toUpperCase)

    assert(!result)
  }
}

object SecretKeyHashUtilsSuite {
  private val TestPassword = "test-p@ssw0rd!"
  private val TestSalt     = "test-s@ltKey1"
}
