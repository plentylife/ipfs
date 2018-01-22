package life.plenty.data

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal


@js.native
@JSGlobal
object Gun extends js.Object {
  def apply(opts: js.Object): Gun = js.native
}

@js.native
trait Gun extends js.Object {
  def get(key: String): Gun = js.native

  def `val`(callback: js.Function2[js.Object, String, Unit]): Gun = js.native

  def map(callback: js.Function1[js.Object, js.Object] = null): Gun = js.native

  def on(callback: js.Function2[js.Object, String, Unit]): Gun = js.native

  def put(data: js.Object, callback: js.Function1[js.Object, Unit] = null): Gun = js.native

  def key(key: String): Gun = js.native

  def set(data: js.Object, callback: js.Function1[js.Object, Unit]): Gun = js.native
}
