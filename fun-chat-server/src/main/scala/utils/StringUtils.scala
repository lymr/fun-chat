package utils
import org.apache.commons.codec.binary.Base64

object StringUtils {

  implicit class StringUtilsExtensions(val self: String) extends AnyVal {
    def stripMargins(margin: String): String = self.stripPrefix(margin).stripSuffix(margin)
  }

  implicit class ByteArrayToBase64StringExtensions(val array: Array[Byte]) extends AnyVal {
    def asBase64(): String = Base64.encodeBase64String(array)
  }

  implicit class ByteArrayFromBase64StringExtensions(val string: String) extends AnyVal {
    def fromBase64(): Array[Byte] = Base64.decodeBase64(string)
  }
}
