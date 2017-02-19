package rest.client.support

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import rest.client.entities.{ClientInformation, UserInformationEntity}
import spray.json.{JsNumber, JsString, _}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val clientInformationFormat = jsonFormat2(ClientInformation)

  implicit object UserInformationEntityJsonFormat extends RootJsonFormat[UserInformationEntity] {
    override def read(json: JsValue): UserInformationEntity = json match {
      case jsObject: JsObject =>
        jsObject.getFields("name", "lastSeen") match {
          case Seq(JsString(name), JsNumber(lastSeen)) =>
            UserInformationEntity(name, new DateTime(lastSeen.toLong))
        }
      case _ => deserializationError("An error occurred while serializing User entity.")
    }

    override def write(obj: UserInformationEntity): JsValue = JsObject(
      "name"     -> JsString(obj.name),
      "lastSeen" -> JsNumber(obj.lastSeen.getMillis)
    )
  }
}
