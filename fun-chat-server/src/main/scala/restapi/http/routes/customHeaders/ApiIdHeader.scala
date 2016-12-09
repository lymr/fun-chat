package restapi.http.routes.customHeaders

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final class ApiIdHeader(id: String) extends ModeledCustomHeader[ApiIdHeader] {
  override def renderInRequests  = false
  override def renderInResponses = false
  override val companion         = ApiIdHeader
  override def value: String     = id
}
object ApiIdHeader extends ModeledCustomHeaderCompanion[ApiIdHeader] {
  override val name                 = "apiIdKey"
  override def parse(value: String) = Try(new ApiIdHeader(value))
}
