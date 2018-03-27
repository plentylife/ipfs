package life.plenty.ui.emailNotification
import life.plenty.data.ReaderInterface
import org.scalajs.dom._

import scala.scalajs.js
import monix.execution.Scheduler.Implicits.{global ⇒ mg}

import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import js.Dynamic.global

object EmailStatusManager {
//  ReaderInterface.LoadIndicator.get.

  private var flagOn = false

  private var trackingSpaces = Map[String, Option[Boolean]]()

  def track(spaceId: String) = if (flagOn) {
    trackingSpaces += spaceId → None
  }
  def track(spaceId: String, nonEmpty: Boolean) = if (flagOn) {
    trackingSpaces += spaceId → Option(nonEmpty)
    send()
  }

  def send() = {
    if (trackingSpaces.valuesIterator.forall(_.nonEmpty)) {
      if (trackingSpaces.values.exists(_.get)) EmailMain.send() else EmailMain.next()
    }
  }

  def turnOn() = {
    flagOn = true
  }

  def isOn = flagOn

}