package core.entities

/**
  * @param password User's secret key
  * @param salt Salt key
  * @param algorithm Hash algorithm
  */
case class CredentialSet(password: String, salt: String, algorithm: String)
