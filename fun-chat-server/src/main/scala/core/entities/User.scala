package core.entities

import org.joda.time.DateTime
import User._

/**
  * @param userId User ID
  * @param name User's name
  * @param password User's encrypted password
  * @param authToken Logged-on user authentication token
  * @param lastSession Last login timestamp, if user is active then value
  *                  is current session start timestamp, otherwise its
  *                  users last session finish timestamp.
  */
case class User(userId: Option[UserID],
                name: String,
                password: String,
                lastSession: DateTime,
                authToken: Option[AuthToken])

object User {
  type UserID    = String
  type AuthToken = String
}
