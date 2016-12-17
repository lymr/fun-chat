package core.authentication

import core.authentication.tokenGenerators.BearerTokenGenerator
import core.db.users.UserCredentialsDao
import core.entities.Defines._
import core.entities.{CredentialSet, User}

class UserAuthenticator(secretValidator: (CredentialSet, String) => Boolean,
                        bearerTokenGenerator: BearerTokenGenerator,
                        credentialsDao: UserCredentialsDao) {

  def authenticate(user: User, password: UserSecret): Option[AuthToken] = {
    for {
      id          <- user.userId
      credentials <- credentialsDao.findUserCredentials(id)
      token       <- if (secretValidator(credentials, password)) bearerTokenGenerator.create(user) else None
    } yield token
  }

  def validateToken(token: AuthToken): Option[User] = {
    bearerTokenGenerator.decode(token)
  }

}
