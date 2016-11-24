package core.authentication

import org.apache.commons.codec.digest.DigestUtils

class SecretKeyUtils() {

  def validate(storedSecret: String, secret: String): Boolean = {
    storedSecret == transform(secret)
  }

  def transform(key: String): String = {
    DigestUtils.md5Hex(key)
  }
}
