package life.plenty.data

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}


@js.native
trait Ack extends js.Object {
  val err: String = js.native
}

@js.native
@JSGlobal
object LevelDB extends LevelUp   {
  def open(): js.Promise[LevelUp] = js.native
}

@js.native
trait LevelUp extends js.Any

class GunConfig (
  var peers: js.UndefOr[js.Object] = js.undefined,
  var file: js.UndefOr[Boolean] = js.undefined,
  var localStorage: js.UndefOr[Boolean] = js.undefined,
  var level: js.UndefOr[LevelUp] = js.undefined
  ) extends js.Object

//{
//var peers: js.UndefOr[js.Object] = js.undefined
//var file: js.UndefOr[Boolean] = js.undefined
//var localStorage: js.UndefOr[Boolean] = js.undefined
//var level: js.UndefOr[LevelUp] = js.undefined
//}

@js.native
trait Gun extends js.Object {
  def get(key: String): Gun = js.native

  def `val`(callback: js.Function2[js.Object, String, Unit], options: js.UndefOr[js.Object]): Gun = js.native

//  def `val`(callback: js.Function2[js.Object, String, Unit]): Gun = js.native

  def map(callback: js.Function1[js.Object, js.Object] = null): Gun = js.native

  def on(callback: js.Function2[js.Object, String, Unit]): Gun = js.native

  def put(data: js.Object, callback: js.Function1[js.Object, Unit] = null): Gun = js.native

  def key(key: String): Gun = js.native

  def set(data: js.Object, callback: js.Function1[js.Object, Unit]): Gun = js.native

  val `_`: Gun_ = js.native
  //  def user(): GunUser = js.native
}

@js.native
trait Gun_ extends js.Object {
  val soul: String = js.native
}

//@js.native
//trait GunUser extends Gun {
//  def create(userName: String, password: String, callback: js.Function1[js.Object, Unit]): Gun = js.native
//  def auth(userName: String, password: String, callback: js.Function1[js.Object, Unit]): Gun = js.native
//  def alive(callback: js.Function0[js.Object]): Future[js.Object] = js.native
//  def recall(validMinutes: Int, callback: js.Function0[js.Object]) = js.native
//}

@js.native
trait GunCalls extends js.Object {
  def getHubClass(id: String, cb: js.Function1[js.Object, Unit]):Unit= js.native
  def get(id: String, cb: js.Function2[js.Object, String, Unit]):Unit= js.native
  def getConnections(id: String, cb: js.Function1[js.Object, Unit]):Unit= js.native
  def mapConnections(id: String, cb: js.Function2[js.Object, String, Unit]):Unit= js.native

  def getInstance: Gun = js.native
}