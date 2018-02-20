package life.plenty

import life.plenty.model.utils.Console

import scala.scalajs.js

package object data {
  val console = new Console(true, true, "Gun")

  var gunCalls: GunCalls = null
}
