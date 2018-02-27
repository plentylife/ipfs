package life.plenty

import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.Console

import scala.scalajs.js

package object data {
  val console = new Console(false, true, "DB")

  var db: ShareDB = null

  /** this is not safe */
  def getWriterModule(hub: Hub) = hub.getTopModule({case m: DbWriterModule => m}).get
}
