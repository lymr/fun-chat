package core.authentication.tokenGenerators

import com.auth0.jwt._
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.typesafe.scalalogging.StrictLogging
import core.entities.{AuthTokenContext, BearerToken, Timer, UserID}
import restapi.http.JsonSupport
import spray.json._

import scala.util.{Failure, Success, Try}

class JwtBearerTokenGenerator(keyGenerator: () => Array[Byte], timer: Timer)
    extends BearerTokenGenerator with JsonSupport with StrictLogging {

  private val secretKey = keyGenerator()

  def create(ctx: AuthTokenContext): Option[BearerToken] = {
    val triedToken = Try {
      val timestamp = timer.freeze
      JWT
        .create()
        .withIssuer("fun-chat")
        .withSubject("auth-bearer")
        .withIssuedAt(timestamp.take)
        .withExpiresAt(timestamp.next)
        .withClaim("uid", ctx.userId.toJson.toString)
        .withClaim("unm", ctx.username)
        .sign(Algorithm.HMAC512(secretKey))
    }

    triedToken match {
      case Success(token) => Some(BearerToken(token))
      case Failure(ex)    => logger.error("Failed to generate token!", ex); None
    }
  }

  def decode(bearer: BearerToken): Option[AuthTokenContext] = {
    verify(bearer).map {
      case (jwt: DecodedJWT) => Seq(jwt.getClaim("uid").asString, jwt.getClaim("unm").asString)
    }.map {
      case Seq(jsId: String, username: String) => AuthTokenContext(jsId.parseJson.convertTo[UserID], username)
    }
  }

  def touch(bearer: BearerToken): Option[BearerToken] = {
    decode(bearer) match {
      case Some(user) => create(user)
      case _          => None
    }
  }

  def isValid(bearer: BearerToken): Boolean = {
    verify(bearer).isDefined
  }

  private def verify(bearer: BearerToken): Option[DecodedJWT] = {
    val triedVerify = Try {

      val verifier = JWT
        .require(Algorithm.HMAC512(secretKey))
        .withIssuer("fun-chat")
        .withSubject("auth-bearer")
        .acceptIssuedAt(timer.intervalStep)
        .acceptExpiresAt(timer.intervalStep)
        .build()

      verifier.verify(bearer.token)
    }

    triedVerify match {
      case Success(jwt) => Some(jwt)
      case Failure(ex)  => logger.error("Failed to verify token!", ex); None
    }
  }
}
