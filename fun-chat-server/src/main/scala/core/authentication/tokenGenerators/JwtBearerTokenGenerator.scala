package core.authentication.tokenGenerators

import java.util.Date

import com.auth0.jwt._
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.typesafe.scalalogging.StrictLogging
import core.entities.Defines.AuthToken
import core.entities.User
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

class JwtBearerTokenGenerator(keyGenerator: () => Array[Byte], tokenExpirationInterval: Long)
    extends BearerTokenGenerator with StrictLogging {

  private val secretKey = keyGenerator()

  def create(user: User): Option[AuthToken] = {
    val triedToken = Try {
      require(user.userId.isDefined)

      val timer = Timer(tokenExpirationInterval)

      JWT
        .create()
        .withIssuer("fun-chat")
        .withSubject("auth-bearer")
        .withIssuedAt(timer.current)
        .withExpiresAt(timer.next)
        .withClaim("uid", user.userId.get)
        .withClaim("unm", user.name)
        .sign(Algorithm.HMAC512(secretKey))
    }

    triedToken match {
      case Success(token) => Some(token)
      case Failure(ex)    => logger.error("Failed to generate token!", ex); None
    }
  }

  def decode(token: AuthToken): Option[User] = {
    verify(token).collect {
      case (jwt: DecodedJWT) => Seq(jwt.getClaim("uid").asString, jwt.getClaim("unm").asString)
    }.map {
      case Seq(id: String, name: String) => User(Some(id), name, DateTime.now)
    }
  }

  def touch(token: AuthToken): Option[AuthToken] = {
    decode(token) match {
      case Some(user) => create(user)
      case _          => None
    }
  }

  def isValid(token: AuthToken): Boolean = {
    verify(token).isDefined
  }

  private def verify(token: AuthToken): Option[DecodedJWT] = {
    val triedVerify = Try {
      val timer = Timer(tokenExpirationInterval)

      val verifier = JWT
      .require(Algorithm.HMAC512(secretKey))
        .withIssuer("fun-chat")
        .withSubject("auth-bearer")
        .acceptIssuedAt(timer.current.getTime)
        .acceptExpiresAt(timer.current.getTime)
        .build()

      verifier.verify(token)
    }

    triedVerify match {
      case Success(jwt) => Some(jwt)
      case Failure(ex)  => logger.error("Failed to verify token!", ex); None
    }
  }
}

/**
  * Timer with constant time interval
  * @param expirationInterval increment time interval in MINUTES
  */
private case class Timer(expirationInterval: Long) {

  private val timestamp = System.currentTimeMillis

  /**
    * Get current timestamp
    * @return
    */
  def current: Date = new Date(timestamp)

  /**
    * Get next interval timestamp, (current + expiration interval)
    * @return next timestamp
    */
  def next: Date = new Date(timestamp + expirationInterval)
}
