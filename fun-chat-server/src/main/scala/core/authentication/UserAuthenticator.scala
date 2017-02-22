package core.authentication

import core.authentication.tokenGenerators.BearerTokenGenerator
import core.db.users.UserCredentialsDao
import core.entities.Defines._
import core.entities._

class UserAuthenticator(secretValidator: (CredentialSet, String) => Boolean,
                        tokenGenerator: BearerTokenGenerator,
                        credentialsDao: UserCredentialsDao) {

  def authenticate(user: User, password: UserSecret): Option[AuthToken] = {
    for {
      creds <- credentialsDao.findUserCredentials(user.userId)
      token <- if (secretValidator(creds, password)) tokenGenerator.create(AuthTokenContext.fromUser(user)) else None
    } yield token
  }

  def validateToken(token: AuthToken): Option[AuthTokenContext] = token match {
    case (bearer: BearerToken) => tokenGenerator.decode(bearer)
    case _                     => None
  }
}
