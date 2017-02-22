package messages.entities

import core.entities.AuthTokenContext
import restapi.http.entities.MessageEntity

case class ForwardUserMessage(message: MessageEntity, recipientName: String, senderCtx: AuthTokenContext)
