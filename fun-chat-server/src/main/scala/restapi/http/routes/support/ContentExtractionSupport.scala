package restapi.http.routes.support

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import restapi.http.JsonSupport
import restapi.http.entities.ClientInformation

private[http] trait ContentExtractionSupport extends JsonSupport {

  def extractClientInfo: Directive1[ClientInformation] = entity(as[ClientInformation])
}
