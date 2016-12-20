package restapi.http.routes.support

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MissingHeaderRejection, Route}
import core.entities.ClientInformation
import restapi.http.routes.ApiContext
import utils.JavaConverters.OptionalToOption

private[http] trait ContentExtractionSupport {

  def extractClientInfo(inner: (ClientInformation) => Route)(implicit apiCtx: ApiContext): Route = {
    extractClientIP { ip =>
      ip.getAddress().toScalaOption match {
        case Some(address) => inner(ClientInformation(address))
        case None          => reject(MissingHeaderRejection("'X-Forwarded-For' / 'Remote-Address' / 'X-Real-Ip'"))
      }
    }
  }
}
