package core.authentication.tokenGenerators

import com.auth0.jwt._
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.typesafe.scalalogging.StrictLogging
import core.authentication.support.AuthenticationJsonSupport
import core.entities._
import spray.json._

import scala.util.{Failure, Success, Try}

class JwtBearerTokenGenerator(keyGenerator: () => SecuredToken, timer: Timer)
    extends BearerTokenGenerator with AuthenticationJsonSupport with StrictLogging {

  private val secretKey = keyGenerator().token

  def create(claims: AuthTokenClaims): Option[BearerToken] = {
    val triedToken = Try {
      val timestamp = timer.freeze
      JWT
        .create()
        .withIssuer("fun-chat")
        .withSubject("auth-bearer")
        .withIssuedAt(timestamp.take)
        .withExpiresAt(timestamp.next)
        .withClaim("uid", claims.userId.toJson.toString)
        .withClaim("unm", claims.username)
        .withClaim("otc", claims.sessionId.toJson.toString())
        .sign(Algorithm.HMAC512(secretKey))
    }

    triedToken match {
      case Success(token) => Some(BearerToken(token))
      case Failure(ex)    => logger.error("Failed to generate token!", ex); None
    }
  }

  def decode(bearer: BearerToken): Option[AuthTokenClaims] = {
    verify(bearer)
      .map {
        case (jwt: DecodedJWT) => Seq(jwt.getClaim("uid").asString, jwt.getClaim("unm").asString, jwt.getClaim("otc"))
      }
      .map {
        case Seq(jsId: String, username: String, sessionId: String) =>
          AuthTokenClaims(jsId.parseJson.convertTo[UserID], username, sessionId.parseJson.convertTo[SessionID])
      }
  }

  //TODO: don't create new token, just update expiration time. Add one time token claim store, update on touch.
  def touch(bearer: BearerToken): Option[BearerToken] = {
    decode(bearer) match {
      case Some(claims) => create(claims)
      case _            => None
    }
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
