package messages.entities

import api.entities.MessageEntity
import core.entities.AuthTokenContext

case class ForwardRawMessage(message: MessageEntity, senderCtx: AuthTokenContext)
