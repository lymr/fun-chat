package messages.entities

import core.entities.TokenContext
import restapi.http.entities.MessageEntity

case class ForwardRawMessage(message: MessageEntity, senderCtx: TokenContext)
