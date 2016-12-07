package core.authentication.tokenGenerators

import java.security.SecureRandom

object SecuredTokenGenerator extends TokenGenerator[Array[Byte]] {
  val TOKEN_LENGTH: Int = 32

  def generate(): Array[Byte] = {
    val srnd = new SecureRandom()
    val buff = new Array[Byte](TOKEN_LENGTH)
    srnd.nextBytes(buff)
    buff
  }
}
