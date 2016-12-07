package core.authentication.tokenGenerators

trait TokenGenerator[A] {

  def generate(): A
}
