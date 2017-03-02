package authentication.fsm

import authentication.fsm.entities.{AuthToken, BearerToken}
import com.typesafe.scalalogging.StrictLogging

object AuthTokenStore extends StrictLogging {

  private var authToken: Option[AuthToken] = None

  def updateToken(token: AuthToken): Unit = token match {
    case BearerToken(bearer) if bearer.nonEmpty => authToken = Some(token)
    case _ => throw new RuntimeException("Received Authentication Token is empty!")
  }

  def getBearerToken: String = authToken match {
    case Some(BearerToken(bearer)) => bearer
    case _ => throw new RuntimeException("Bearer Token is empty!")
  }
}
