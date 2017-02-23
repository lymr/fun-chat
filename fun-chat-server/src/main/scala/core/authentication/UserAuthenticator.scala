package core.authentication

import core.authentication.tokenGenerators.BearerTokenGenerator
import core.db.users.UserCredentialsDao
import core.entities._

class UserAuthenticator(secretValidator: (CredentialSet, UserSecret) => Boolean,
                        tokenGenerator: BearerTokenGenerator,
                        credentialsDao: UserCredentialsDao) {

  def authenticate(user: User, secret: UserSecret): Option[AuthToken] = {
    for {
      creds <- credentialsDao.findUserCredentials(user.userId)
      token <- if (secretValidator(creds, secret)) tokenGenerator.create(AuthTokenContext.fromUser(user)) else None
    } yield token
  }

  def validateToken(token: AuthToken): Option[AuthTokenContext] = token match {
    case (bearer: BearerToken) => tokenGenerator.decode(bearer)
    case _                     => None
  }
}
