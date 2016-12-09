package restapi.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

private[http] trait ContentExtractionSupport {

  def extractUserInfo(internal: (String, String) => Route): Route = {
    headerValueByName("apiIdKey") { id =>
      headerValueByName("apiTokenKey") { token =>
        internal(id, token)
      }
    }
  }

}
