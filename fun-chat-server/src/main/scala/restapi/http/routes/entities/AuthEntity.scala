package restapi.http.routes.entities

import core.entities.Defines.AuthToken

private[http] case class AuthEntity(login: String, token: AuthToken)
