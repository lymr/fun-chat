package restapi.http

import core.entities._
import org.joda.time.DateTime
import spray.json._
import support.JsonApiSupport

trait JsonSupport extends JsonApiSupport {

  implicit val userIdFormat = jsonFormat1(UserID)

  implicit object UserJsonFormat extends RootJsonFormat[User] {
    override def read(json: JsValue): User = json match {
      case jsObject: JsObject =>
        jsObject.getFields("userId", "name", "lastSeen") match {
          case Seq(userId, JsString(name), JsNumber(lastSeen)) =>
            User(userId.convertTo[UserID], name, new DateTime(lastSeen.toLong))
        }
      case _ => deserializationError("An error occurred while serializing User entity.")
    }

    override def write(obj: User): JsValue = JsObject(
      "userId"   -> obj.userId.toJson,
      "name"     -> JsString(obj.name),
      "lastSeen" -> JsNumber(obj.lastSeen.getMillis)
    )
  }

  implicit object AuthTokenFormat extends RootJsonFormat[AuthToken] {
    override def read(json: JsValue): AuthToken =
      deserializationError("Operation not supported.")

    override def write(obj: AuthToken): JsValue = obj match {
      case BearerToken(bearer) => JsObject("bearer" -> JsString(bearer))
      case _                   => deserializationError("Unsupported AuthToken.")
    }
  }
}
