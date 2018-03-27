package life.plenty.ui.emailNotification
import life.plenty.data.ReaderInterface
import org.scalajs.dom._

import scala.scalajs.js
import monix.execution.Scheduler.Implicits.{global ⇒ mg}

import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import js.Dynamic.global

object EmailManager {
//  ReaderInterface.LoadIndicator.get.

  private var flagOn = false

  def send(events: List[_]) = {
    if (events.nonEmpty) EmailMain.send() else EmailMain.next()
  }

  def turnOn() = {
    flagOn = true
  }

  def isOn = flagOn

}