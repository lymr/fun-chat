package core.authentication

import core.authentication.tokenGenerators.{BearerTokenGenerator, SecuredTokenGenerator}
import core.db.users.UserCredentialsDao
import core.entities._
import utils.StringUtils._

class UserAuthenticator(secretValidator: (CredentialSet, UserSecret) => Boolean,
                        tokenGenerator: BearerTokenGenerator,
                        credentialsDao: UserCredentialsDao) {

  private var userClaims: Map[UserID, SessionID] = Map.empty

  def authenticate(user: User, secret: UserSecret): Option[AuthToken] = {
    for {
      creds <- credentialsDao.findUserCredentials(user.userId)
      token <- if (secretValidator(creds, secret)) generateAuthToken(user) else None
    } yield token
  }

  def validateToken(token: AuthToken): Option[AuthTokenContext] = token match {
    case (bearer: BearerToken) =>
      tokenGenerator
        .decode(bearer)
        .filter { decodedClaims =>
          userClaims.get(decodedClaims.userId).contains(decodedClaims.sessionId)
        }
        .map { decodedClaims =>
          AuthTokenContext(decodedClaims.userId, decodedClaims.username)
        }
    case _ => None
  }

  private def generateAuthToken(user: User): Option[AuthToken] = {
    val onetimeClaim = SecuredTokenGenerator.generate().token.asBase64()
    val token        = tokenGenerator.create(AuthTokenClaims(user.userId, user.name, SessionID(onetimeClaim)))
    if (token.isDefined) {
      userClaims = userClaims + (user.userId -> SessionID(onetimeClaim))
    }
    token
  }

  def revokeToken(userId: UserID): Unit = {
    userClaims = userClaims - userId
  }
}
