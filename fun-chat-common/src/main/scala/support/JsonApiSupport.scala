package support

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import api.entities.MessageProcessingCodes.MessageProcessingCode
import api.entities._
import org.joda.time.DateTime
import spray.json._

trait JsonApiSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val messageEntityFormat         = jsonFormat2(MessageEntity)
  implicit val clientInformationFormat     = jsonFormat1(ClientInformation)
  implicit val messageProcessingCodeFormat = jsonFormat2(MessageProcessingCode)

  implicit object MessageProcessingResponseFormat extends RootJsonFormat[MessageProcessingResponse] {

    override def read(json: JsValue): MessageProcessingResponse = json match {
      case jsObject: JsObject =>
        jsObject.getFields("code", "message") match {
          case Seq(code, messageOpt) =>
            MessageProcessingResponse(code.convertTo[MessageProcessingCode], messageOpt.convertTo[Option[String]])
        }
      case other => deserializationError(s"An error occurred while serializing entity $other.")
    }

    override def write(obj: MessageProcessingResponse): JsValue =
      JsObject("code" -> obj.code.toJson, "message" -> obj.message.toJson)
  }

  implicit object UserInformationEntityJsonFormat extends RootJsonFormat[UserInformationEntity] {
    override def read(json: JsValue): UserInformationEntity = json match {
      case jsObject: JsObject =>
        jsObject.getFields("name", "lastSeen") match {
          case Seq(JsString(name), JsNumber(lastSeen)) =>
            UserInformationEntity(name, new DateTime(lastSeen.toLong))
        }
      case other => deserializationError(s"An error occurred while serializing entity $other.")
    }

    override def write(obj: UserInformationEntity): JsValue = JsObject(
      "name"     -> JsString(obj.name),
      "lastSeen" -> JsNumber(obj.lastSeen.getMillis)
    )
  }

}
