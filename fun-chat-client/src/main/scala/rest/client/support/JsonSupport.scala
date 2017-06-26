package rest.client.support

import authentication.entities.{AuthToken, BearerToken}
import spray.json.{JsString, _}
import support.JsonApiSupport

trait JsonSupport extends JsonApiSupport {

  implicit object AuthTokenFormat extends RootJsonFormat[AuthToken] {
    override def read(json: JsValue): AuthToken = json match {
      case jsObject: JsObject =>
        jsObject.getFields("bearer") match {
          case Seq(JsString(bearer)) => BearerToken(bearer)
        }
      case _ => deserializationError("An error occurred while deserialize entity.")
    }

    override def write(obj: AuthToken): JsValue = obj match {
      case _ => deserializationError("Operation not supported.")
    }
  }
}
