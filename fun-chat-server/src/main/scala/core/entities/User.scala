package core.entities

import org.joda.time.DateTime

/**
  * @param userId User ID
  * @param name User's name
  * @param lastSeen Last operation timestamp
  */
case class User(userId: UserID, name: String, lastSeen: DateTime)

/**
  * Hold user's unique ID
  * @param id User's unique iD
  */
case class UserID(id: String)
