package core.authentication.tokenGenerators

import core.entities.{AuthTokenContext, BearerToken}

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
  def create(ctx: AuthTokenContext): Option[BearerToken]

  /**
    * Decodes given Authentication token and extracts underlying user information.
    * @param bearer Given token
    * @return Token information
    */
  def decode(bearer: BearerToken): Option[AuthTokenContext]

  /**
    * Extends token expiration timestamp with
    * @param bearer Given token
    * @return Authentication token
    */
  def touch(bearer: BearerToken): Option[BearerToken]

  /**
    * Checks whether given token is valid.
    * @param bearer Given token
    * @return True if token validated correctly, otherwise False
    */
  def isValid(bearer: BearerToken): Boolean
}
