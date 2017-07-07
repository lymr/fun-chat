package core.authentication.tokenGenerators

import core.entities.{AuthTokenClaims, BearerToken}

/**
  * OAuth2 Bearer token generator.
  * - Backed with Auth0 Json Web Token.
  */
trait BearerTokenGenerator {

  /**
    * Creates a JWT (Json Web Token) Authentication token with User information
    * @param ctx Given TokenContext
    * @return Authentication token
    */
  def create(ctx: AuthTokenClaims): Option[BearerToken]

  /**
    * Decodes given Authentication token and extracts underlying user information.
    * @param bearer Given token
    * @return Token information
    */
  def decode(bearer: BearerToken): Option[AuthTokenClaims]

  /**
    * Extends token expiration timestamp with
    * @param bearer Given token
    * @return Authentication token
    */
  def touch(bearer: BearerToken): Option[BearerToken]
}
