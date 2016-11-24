object Main extends App {

  try {
    val myBackupBootstrapper = new Bootstrap()
    myBackupBootstrapper.startup()
  } catch {
    case ex: Exception =>
      println(s"Unexpected error occurred, Message ${ex.getMessage}.")
      ex.printStackTrace()
  }
}
