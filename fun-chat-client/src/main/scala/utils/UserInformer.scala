package utils

trait UserInformer {

  /**
    * Displays a message to user
    * @param message given message to display.
    */
  def informUserCallback(message: String): Unit = {
    println(message)
  }

}
