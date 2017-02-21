package restapi.http.routes.support

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.entities.{TokenContext, User}
import restapi.http.routes.ApiContext

import scala.util.Success

private[http] trait SecuredAccessSupport {

  def securedAccess(inner: (TokenContext) => Route)(implicit apiCtx: ApiContext): Route = {
    extractCredentials {
      case Some(OAuth2BearerToken(token)) =>
        val ctxFuture = apiCtx.authenticate(token)
        onComplete(ctxFuture) {
          case Success(Some(ctx)) => inner(ctx)
          case _                  => complete(StatusCodes.Unauthorized)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }
  }

  def privateResourceAccess(ctx: TokenContext, username: String)(inner: Route)(implicit apiCtx: ApiContext): Route = {
    apiCtx.findUserByName(username) match {
      case Some(User(id, _, _)) if id.equals(ctx.userId) => inner
      case _                                                   => complete(StatusCodes.NotAcceptable)
    }
  }
}
