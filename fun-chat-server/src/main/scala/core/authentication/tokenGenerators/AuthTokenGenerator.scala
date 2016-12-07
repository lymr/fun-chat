package core.authentication.tokenGenerators

import java.util.UUID

import core.entities.Defines.AuthToken

object AuthTokenGenerator extends TokenGenerator[AuthToken] {

  def generate(): AuthToken = {
    UUID.randomUUID().toString
  }
}
