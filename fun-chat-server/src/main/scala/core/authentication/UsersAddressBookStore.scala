package core.authentication

import java.net.InetAddress

import core.entities.Defines.UserID

import scala.collection.mutable

/**
  * Stores user's clients IP address.
  */
class UsersAddressBookStore() extends UsersAddressBook with UserConnectionStatus {

  private val usersData: mutable.Map[UserID, InetAddress] = mutable.Map().empty

  def find(userId: UserID): Option[InetAddress] = {
    usersData.get(userId)
  }

  def update(userId: UserID, address: InetAddress): Unit = {
    usersData.update(userId, address)
  }

  def remove(userId: UserID): Unit = {
    usersData.remove(userId)
  }

  override def isOnline(userId: UserID): Boolean = {
    usersData.contains(userId)
  }
}

trait UserConnectionStatus {
  def isOnline(userId: UserID): Boolean
}

trait UsersAddressBook {

  def find(userId: UserID): Option[InetAddress]

  def update(userId: UserID, address: InetAddress): Unit

  def remove(userId: UserID): Unit
}
