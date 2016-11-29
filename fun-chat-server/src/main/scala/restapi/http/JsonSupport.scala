package restapi.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import core.entities.User
import core.entities.Defines._
import org.joda.time.DateTime
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object UserJsonFormat extends JsonFormat[User] {
    override def read(json: JsValue): User = json match {
      case jsObject: JsObject =>
        jsObject.getFields("userId", "name", "lastSeen") match {
          case Seq(userId, JsString(name), JsNumber(lastSeen)) =>
            User(userId.convertTo[Option[UserID]], name, new DateTime(lastSeen))
        }
      case _ => deserializationError("An error occurred while serializing User entity.")
    }

    override def write(obj: User): JsValue = JsObject(
      "userId"   -> obj.userId.toJson,
      "name"     -> JsString(obj.name),
      "lastSeen" -> JsNumber(obj.lastSeen.getMillis)
    )
  }
}
