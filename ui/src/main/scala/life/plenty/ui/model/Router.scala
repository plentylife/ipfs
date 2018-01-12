package life.plenty.ui.model

import java.util.Base64

import com.thoughtworks.binding.Route
import life.plenty.ui.model.DisplayModel.ModuleOverride
import upickle.default.{macroRW, ReadWriter ⇒ RW, _}

object Router {
  Route.Hash(List[ModuleOverride]())(new Route.Format[List[ModuleOverride]] {
    override def unapply(hashText: String): Option[List[ModuleOverride]] = ???
    override def apply(state: List[ModuleOverride]): String = ???
  })

  def toHash(r: RoutingParams): String = {
    Base64.getEncoder.encodeToString(write(r).getBytes)
  }

  def fromHash(h: String) = read[RoutingParams](Base64.getDecoder.decode(h).map(_.toChar).mkString)
}

object ViewState extends Enumeration {
  type ViewState = Value
  val DISCUSSION, RATING = Value
}

case class RoutingParams(stateId: Int) {
  def state = ViewState(stateId)
}

object RoutingParams {
  //  implicit val viewStateWriter: Writer[ViewState] = Writer(vs ⇒ writeJs(vs.id) )
  implicit def rw: RW[RoutingParams] = macroRW
}
