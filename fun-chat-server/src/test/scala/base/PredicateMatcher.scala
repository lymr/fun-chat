package base

import org.mockito.ArgumentMatcher

case class PredicateMatcher[A](predicate: (A) => Boolean) extends ArgumentMatcher[A] {
  override def matches(argument: scala.Any): Boolean = {
    argument match {
      case (arg: A) => predicate(arg)
      case _        => false
    }
  }
}
