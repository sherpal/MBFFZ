package gamemanager


import messages.Message
import messages.Message.{Ping, Pong}

import scala.scalajs.js.timers.setTimeout

final class Synchronization(messageSender: Message => Unit) {

  private var delta: Long = 0

  private var deltaRecords: List[Long] = Nil
  private val totalNumberOfTrials = 30
  private var numberOfTrials = 0
  private var _synchronized: Boolean = false
  private val delayForComputingLinkTime: Int = 30

  def synchronized: Boolean = _synchronized

  def time: Long = new java.util.Date().getTime + delta

  private def endComputingCallback(): Unit = {
    println(s"Delta was computed: $delta")
  }

  private def ping(): Unit = {
    messageSender(Ping(new java.util.Date().getTime))
  }

  def computeLinkTime(): Unit = {
    numberOfTrials = 0
    deltaRecords = Nil
    ping()
  }

  def receivePong(pong: Pong): Unit = if (!_synchronized) {
    val currentTime = new java.util.Date().getTime
    val latency = (currentTime - pong.sendingTime) / 2
    val linkTime = pong.midwayTime + latency
    deltaRecords = (linkTime - currentTime) +: deltaRecords

    numberOfTrials += 1

    if (numberOfTrials >= totalNumberOfTrials) {
      val nbrRecords = deltaRecords.length.toDouble
      val mean = deltaRecords.sum / nbrRecords
      val std = deltaRecords.map(t => (t - mean) * (t - mean)).sum / nbrRecords
      val relevantData = if (std < 0.0001) deltaRecords else deltaRecords.filter(t => {
        // simple anomaly detection
        // we assume latency is normal distributed, which is probably wrong (chi-squared should fit better)
        // we take only data in [-x_0,x_0] where x_0 is such that P(X < x_0) < 1/20.
        val normalized = (t - mean) / std
        normalized > -1.6449 && normalized < 1.6449
      })
      if (relevantData.isEmpty) {
        computeLinkTime()
      } else {
        _synchronized = true

        println(relevantData)

        delta = relevantData.sum / relevantData.length
        endComputingCallback()
      }
    } else {
      setTimeout(delayForComputingLinkTime) {
        ping()
      }
    }

  }

}
