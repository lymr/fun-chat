package restapi.http.routes.customHeaders

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final class ApiTokenHeader(token: String) extends ModeledCustomHeader[ApiTokenHeader] {
  override def renderInRequests  = false
  override def renderInResponses = false
  override val companion         = ApiTokenHeader
  override def value: String     = token
}
object ApiTokenHeader extends ModeledCustomHeaderCompanion[ApiTokenHeader] {
  override val name                 = "apiTokenKey"
  override def parse(value: String) = Try(new ApiTokenHeader(value))
}
