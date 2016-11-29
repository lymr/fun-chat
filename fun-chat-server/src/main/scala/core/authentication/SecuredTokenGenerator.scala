package core.authentication

import java.security.SecureRandom

object SecuredTokenGenerator {
  val TOKEN_LENGTH: Int = 32

  def generateString(): String = {
    generate().toString
  }

  def generate(): Array[Char] = {
    val srnd = new SecureRandom()
    val buff = new Array[Byte](TOKEN_LENGTH)
    srnd.nextBytes(buff)
    buff.map(_.toChar)
  }
}
