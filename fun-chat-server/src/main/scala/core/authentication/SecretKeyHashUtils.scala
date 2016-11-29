package core.authentication

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import com.typesafe.scalalogging.StrictLogging
import core.entities.CredentialSet

import scala.util.{Failure, Success, Try}

object SecretKeyHashUtils extends StrictLogging {

  val HASH_ALGORITHM: String = "PBKDF2WithHmacSHA512"
  val ITERATIONS: Int        = 20
  val SECRET_KEY_LENGTH: Int = 64

  def validate(storedCredentials: CredentialSet, password: String): Boolean = {
    val given = calculateHash(password.toCharArray, storedCredentials.salt.map(_.toByte))
    given match {
      case Some(givenPassword) => storedCredentials.password.sameElements(givenPassword)
      case None              => false
    }
  }

  def generate(password: Array[Char], salt: Array[Char]): Option[CredentialSet] = {
    val result = calculateHash(password, salt.map(_.toByte))
    result.map(s => CredentialSet(s.map(_.toChar), salt, HASH_ALGORITHM.toCharArray))
  }

  private def calculateHash(password: Array[Char],
                            salt: Array[Byte],
                            iterations: Int = ITERATIONS,
                            keyLength: Int = SECRET_KEY_LENGTH): Option[Array[Byte]] = {

    val hashCalculation = Try[Array[Byte]] {
      val keyFactory = SecretKeyFactory.getInstance(HASH_ALGORITHM)
      val keySpec    = new PBEKeySpec(password, salt, iterations, keyLength)
      val secretKey  = keyFactory.generateSecret(keySpec)
      secretKey.getEncoded()
    }

    hashCalculation match {
      case Success(value) => Some(value)
      case Failure(err)   => logger.error("Operation failed!", err); None
    }
  }
}
