package tests

import org.scalatest.{AsyncWordSpecLike, Matchers, WordSpecLike}
import tests.support.MockitoSupport

trait TestWordSpec extends WordSpecLike with Matchers with MockitoSupport

trait AsyncTestWordSpec extends AsyncWordSpecLike with Matchers with MockitoSupport
