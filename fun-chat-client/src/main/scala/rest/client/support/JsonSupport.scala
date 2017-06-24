package rest.client.support

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import authentication.entities.{AuthToken, BearerToken}
import rest.client.entities.{ClientInformation, UserInformationEntity}
import spray.json.{JsNumber, JsString, _}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val clientInformationFormat = jsonFormat2(ClientInformation)

  implicit object UserInformationEntityJsonFormat extends RootJsonFormat[UserInformationEntity] {
    override def read(json: JsValue): UserInformationEntity = json match {
      case jsObject: JsObject =>
        jsObject.getFields("name", "lastSeen") match {
          case Seq(JsString(name), JsNumber(lastSeen)) =>
            UserInformationEntity(name, lastSeen.toLong)
        }
      case _ => deserializationError("An error occurred while serializing User entity.")
    }

    override def write(obj: UserInformationEntity): JsValue = JsObject(
      "name"     -> JsString(obj.name),
      "lastSeen" -> JsNumber(obj.lastSeen)
    )
  }

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
