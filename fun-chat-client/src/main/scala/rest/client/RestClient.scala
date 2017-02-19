package rest.client

import rest.client.entities.UserInformationEntity

trait RestClient {

  def options(): Unit

  def signIn(username: String, password: String): Unit

  def signUp(username: String, password: String): Unit

  def signOut(): Unit

  def listOnlineUsers(): Seq[UserInformationEntity]

  def sendMessage(recipient: String, content: String): Unit
}




