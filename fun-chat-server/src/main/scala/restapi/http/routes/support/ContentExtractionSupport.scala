package restapi.http.routes.support

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import api.entities.ClientInformation
import restapi.http.JsonSupport

private[http] trait ContentExtractionSupport extends JsonSupport {

  def extractClientInfo: Directive1[ClientInformation] = entity(as[ClientInformation])
}
