package messages.entities

import core.entities.AuthTokenContext
import restapi.http.entities.MessageEntity

case class ForwardRawMessage(message: MessageEntity, senderCtx: AuthTokenContext)
