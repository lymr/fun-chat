package core.entities

import java.util.Date

import core.entities.Timer._

import scala.concurrent.duration.FiniteDuration

/**
  * Timer with constant time interval
  * @param intervalStep increment time interval step in Seconds
  */
case class Timer(intervalStep: Long = DEFAULT_INTERVAL_STEP) {

  /**
    * Returns current timestamp as IntervalTimestamp
    */
  def freeze: IntervalTimestamp = IntervalTimestamp(System.currentTimeMillis, intervalStep)
}

/**
  * Representation of a interval timestamp
  * @param timestamp instantiation timestamp
  * @param intervalStep increment time interval step in Seconds
  */
case class IntervalTimestamp(timestamp: Long, intervalStep: Long) {

  /**
    * Get instantiation timestamp
    * @return Date object representing instantiation timestamp
    */
  def take: Date = new Date(timestamp)

  /**
    * Get next timestamp since instantiation timestamp
    * @return Date object representing next timestamp
    */
  def next: Date = new Date(timestamp + intervalStep * 1000)

  /**
    * Increments current instantiation timestamp with interval step
    * @return IntervalTimestamp instance set to next timestamp
    */
  def increment: IntervalTimestamp = IntervalTimestamp(timestamp + intervalStep * 1000, intervalStep)
}

object Timer {
  //TODO: Should be taken from configuration
  val DEFAULT_INTERVAL_STEP: Long = FiniteDuration(90, "minutes").toSeconds
}
