package restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.entities.User

import scala.util.{Failure, Success}

private[http] trait SecuredAccessSupport {

  def securedAccess(internalRoute: (User) => Route)(implicit context: AuthorizationContext): Route = {
    extractCredentials {
      case Some(OAuth2BearerToken(token)) =>
        val userFuture = context.tokenAuthorizer(token)
        onComplete(userFuture) {
          case Success(Some(user)) => internalRoute(user)
          case Success(_)          => complete(StatusCodes.Unauthorized)
          case Failure(ex)         => complete(StatusCodes.Unauthorized, ex.getMessage)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }
  }

  def privateResourceAccess(ctx: User, username: String)(internalRoute: Route)(
      implicit context: AuthorizationContext): Route = {

    context.findUserByName(username) match {
      case Some(User(id, _, _)) if id.equals(ctx.userId) => internalRoute
      case _                                             => complete(StatusCodes.Unauthorized)
    }
  }

}
