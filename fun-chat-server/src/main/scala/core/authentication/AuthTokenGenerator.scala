package core.authentication

import java.util.UUID

import core.entities.User.AuthToken

class AuthTokenGenerator {

  def generate(): AuthToken = {
    UUID.randomUUID().toString
  }
}
