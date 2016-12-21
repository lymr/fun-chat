package core.entities

import base.TestSuite
import core.entities.TimerSuite._
import org.joda.time.DateTime

class TimerSuite extends TestSuite {

  test("interval validation") {
    val timer = Timer(DEFAULT_INTERVAL)

    val result = timer.freeze

    assertResult(result.intervalStep)(DEFAULT_INTERVAL)
  }

  test("increment interval validation") {
    val timer = Timer(DEFAULT_INTERVAL)

    val result = timer.freeze.increment

    assertResult(result.intervalStep)(DEFAULT_INTERVAL)
  }

  test("check timestamp") {
    val timer     = Timer(DEFAULT_INTERVAL)
    val timestamp = timer.freeze

    val result = timestamp.take

    assert(System.currentTimeMillis - result.getTime < 100)
  }

  test("check next interval") {
    val timer     = Timer(DEFAULT_INTERVAL)
    val timestamp = timer.freeze

    val result = timestamp.next

    val expected = new DateTime(timestamp.take.getTime).plusSeconds(DEFAULT_INTERVAL.toInt)
    assertResult(expected.getMillis)(result.getTime)
  }
}

object TimerSuite {
  val DEFAULT_INTERVAL: Long = 120
}
