package restapi.http.entities

private[http] case class MessageEntity(content: String, sender: String, recipients: Seq[String], timestamp: Long)
