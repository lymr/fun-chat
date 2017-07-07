package core.authentication.support

import core.entities.SessionID
import restapi.http.JsonSupport

trait AuthenticationJsonSupport extends JsonSupport {

  implicit val sessionIdFormat = jsonFormat1(SessionID)
}
