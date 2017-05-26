package core.authentication

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import com.typesafe.scalalogging.StrictLogging
import core.entities.CredentialSet
import utils.StringUtils.{ByteArrayFromBase64StringExtensions, ByteArrayToBase64StringExtensions}

import scala.util.{Failure, Success, Try}

/**
  * Generates a Hash array of bytes representing an encrypted sequence of Salt and Secret keys.
  */
object SecretKeyHashUtils extends StrictLogging {

  private val HASH_ALGORITHM: String = "PBKDF2WithHmacSHA512"
  private val ITERATIONS: Int        = 64
  private val SECRET_KEY_LENGTH: Int = 512

  def validate(storedCredentials: CredentialSet, secret: String): Boolean = {
    val given = calculateHash(secret.toCharArray, storedCredentials.salt.fromBase64()).map(_.asBase64())
    given match {
      case Some(givenPassword) => storedCredentials.password.equals(givenPassword)
      case None                => false
    }
  }

  def generate(password: Array[Char], salt: Array[Byte]): Option[CredentialSet] = {
    val result = calculateHash(password, salt)
    result.map(pw => CredentialSet(pw.asBase64(), salt.asBase64(), HASH_ALGORITHM))
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
