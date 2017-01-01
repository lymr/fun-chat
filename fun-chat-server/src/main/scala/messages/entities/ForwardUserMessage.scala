package messages.entities

import core.entities.TokenContext
import restapi.http.entities.MessageEntity

case class ForwardUserMessage(message: MessageEntity, recipientName: String, senderCtx: TokenContext)
