package core.authentication

import core.authentication.tokenGenerators.BearerTokenGenerator
import core.db.users.UserCredentialsDao
import core.entities.Defines._
import core.entities.{CredentialSet, TokenContext, User}

class UserAuthenticator(secretValidator: (CredentialSet, String) => Boolean,
                        tokenGenerator: BearerTokenGenerator,
                        credentialsDao: UserCredentialsDao) {

  def authenticate(user: User, password: UserSecret): Option[AuthToken] = {
    for {
      creds <- credentialsDao.findUserCredentials(user.userId)
      token <- if (secretValidator(creds, password)) tokenGenerator.create(TokenContext.fromUser(user)) else None
    } yield token
  }

  def validateToken(token: AuthToken): Option[TokenContext] = {
    tokenGenerator.decode(token)
  }
}
