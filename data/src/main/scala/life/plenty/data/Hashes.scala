package life.plenty.data

import life.plenty.model.utils.Hash

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@JSGlobal
@js.native
object Hashes extends js.Any {

  val SHA256 = js.constructorTag[SHA256]
}

@js.native
class SHA256 extends js.Any {
  //  def apply(): SHA256 = js.
  def b64(str: String): String = js.native
}

object DataHash extends Hash {
  private val h = Hashes.SHA256.newInstance()

  override def b64(str: String): String = h.b64(str)
}