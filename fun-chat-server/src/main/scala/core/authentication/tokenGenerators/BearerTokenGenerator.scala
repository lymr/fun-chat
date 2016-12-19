package core.authentication.tokenGenerators

import core.entities.Defines.AuthToken
import core.entities.TokenContext

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
  def create(ctx: TokenContext): Option[AuthToken]

  /**
    * Decodes given Authentication token and extracts underlying user information.
    * @param token Given token
    * @return Token information
    */
  def decode(token: AuthToken): Option[TokenContext]

  /**
    * Extends token expiration timestamp with
    * @param token Given token
    * @return Authentication token
    */
  def touch(token: AuthToken): Option[AuthToken]

  /**
    * Checks whether given token is valid.
    * @param token Given token
    * @return True if token validated correctly, otherwise False
    */
  def isValid(token: AuthToken): Boolean
}
