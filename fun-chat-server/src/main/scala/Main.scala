object Main extends App {

  try {
    val funChatBootstrapping = new Bootstrap()
    funChatBootstrapping.startup()
  } catch {
    case ex: Exception =>
      println(s"Unexpected error occurred, Message ${ex.getMessage}.")
      ex.printStackTrace()
  }
}
