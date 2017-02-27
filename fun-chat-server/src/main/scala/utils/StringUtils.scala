package utils

object StringUtils {

  implicit class StringUtilsExtensions(val self: String) extends AnyVal {
    def stripMargins(margin: String): String = self.stripPrefix(margin).stripSuffix(margin)
  }
}
