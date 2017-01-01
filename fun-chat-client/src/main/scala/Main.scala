import com.typesafe.scalalogging.StrictLogging

object Main extends App with StrictLogging {

  try {
    val funChatClientBootstrapping = new Bootstrap()
    funChatClientBootstrapping.startup()
  } catch {
    case ex: Exception => logger.error("Unexpected error occurred!", ex)
  }
}
