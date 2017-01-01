package commands

trait ClientCommand {

  /**
    * Command name
    */
  val command: String

  /**
    * Executes command with args.
    * @param args Given args list.
    */
  def execute(args: Any*): Unit
}
