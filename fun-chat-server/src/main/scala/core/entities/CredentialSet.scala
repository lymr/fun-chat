package core.entities

/**
  * @param password User's secret key
  * @param salt Salt key
  * @param algorithm Hash algorithm
  */
case class CredentialSet(password: Array[Char], salt: Array[Char], algorithm: Array[Char])
