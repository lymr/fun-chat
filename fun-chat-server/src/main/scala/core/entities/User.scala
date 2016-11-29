package core.entities

import org.joda.time.DateTime
import Defines._

/**
  * @param userId User ID
  * @param name User's name
  * @param lastSeen Last operation timestamp
  */
case class User(userId: Option[UserID], name: String, lastSeen: DateTime)
