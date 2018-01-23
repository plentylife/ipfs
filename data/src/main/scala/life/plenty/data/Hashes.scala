package life.plenty.data

import life.plenty.model.utils.Hash

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@JSGlobal
@js.native
object Hashes extends js.Any {

  @js.native
  class SHA256 extends js.Any {
    def b64(str: String): String = js.native
  }
}


object DataHash extends Hash {
  private val h = new Hashes.SHA256

  override def b64(str: String): String = h.b64(str)
}