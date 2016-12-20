package base

import org.hamcrest.Matcher
import org.mockito.MockitoAnnotations
import org.mockito.verification.VerificationMode
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FunSuiteLike, Suite}

trait TestSuite extends FunSuiteLike with MockitoSuite

trait MockitoSuite extends Suite with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    MockitoAnnotations.initMocks(this)
  }

  def eq[A](value: A) = {
    org.mockito.Matchers.eq[A](value)
  }

  def any[A] = {
    org.mockito.Matchers.any[A]
  }

  def argThat[A](matcher: Matcher[A]) = {
    org.mockito.Matchers.argThat[A](matcher)
  }

  def when[A](methodCall: A) = {
    org.mockito.Mockito.when[A](methodCall)
  }

  def verify[A](mock: A) = {
    org.mockito.Mockito.verify(mock: A)
  }

  def verify[A](mock: A, mode: VerificationMode) = {
    org.mockito.Mockito.verify(mock, mode)
  }

  def times(wantedNumberOfInvocations: Int) = {
    org.mockito.Mockito.times(wantedNumberOfInvocations)
  }
}
