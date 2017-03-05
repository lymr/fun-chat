package base

import org.mockito.ArgumentMatcher

import scala.util.Try

case class PredicateMatcher[A](predicate: (A) => Boolean) extends ArgumentMatcher[A] {
  override def matches(argument: scala.Any): Boolean = {
    Try {
      predicate(argument.asInstanceOf[A])
    }.recover {
      case _: Exception => false
    }.getOrElse(false)
  }
}
