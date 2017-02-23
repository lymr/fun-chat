package core.authentication.tokenGenerators

import java.security.SecureRandom

import core.entities.SecuredToken

object SecuredTokenGenerator {
  val TOKEN_LENGTH: Int = 32

  def generate(): SecuredToken = {
    val srnd = new SecureRandom()
    val buff = new Array[Byte](TOKEN_LENGTH)
    srnd.nextBytes(buff)
    SecuredToken(buff)
  }
}
