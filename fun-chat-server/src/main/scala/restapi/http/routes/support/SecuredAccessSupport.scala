package restapi.http.routes.support

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.entities.{AuthTokenContext, BearerToken, User}
import restapi.http.routes.ApiContext

private[http] trait SecuredAccessSupport {

  def securedAccess(inner: AuthTokenContext => Route)(implicit apiCtx: ApiContext): Route = {
    extractCredentials {
      case Some(OAuth2BearerToken(token)) =>
        apiCtx.authenticate(BearerToken(token)) match {
          case Some(ctx) => inner(ctx)
          case None      => complete(StatusCodes.Unauthorized)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }
  }

  def privateResourceAccess(ctx: AuthTokenContext, username: String)(inner: Route)(implicit apiCtx: ApiContext): Route = {
    apiCtx.findUserByName(username) match {
      case Some(User(id, _, _)) if id.equals(ctx.userId) => inner
      case _                                             => complete(StatusCodes.NotAcceptable)
    }
  }
}
