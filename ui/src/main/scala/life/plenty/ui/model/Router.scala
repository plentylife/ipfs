package life.plenty.ui.model

import java.util.Base64

import com.thoughtworks.binding.Route
import life.plenty.ui.model.ViewState.ViewState
import org.scalajs.dom.window
import upickle.default.{macroRW, ReadWriter ⇒ RW, _}

import scala.util.Try

object Router {
  val router: Route.Hash[RoutingParams] = Route.Hash(defaultRoutingParams)(new Route.Format[RoutingParams] {
    override def unapply(hashText: String): Option[RoutingParams] = {
      val r = fromHash(hashText)
      println("read hash", r)
      r
    }

    override def apply(state: RoutingParams): String = toHash(state)
  })

  private def defaultRoutingParams = fromHash(window.location.hash) getOrElse changeViewState(ViewState.DISCUSSION)

  var paramsOnLoad: Option[RoutingParams] = None
  def initialize = {
    paramsOnLoad = fromHash(window.location.hash)
    router.watch()
  }

  def toHash(r: RoutingParams): String = {
    "#" + Base64.getEncoder.encodeToString(write(r).getBytes)
  }
  def fromHash(h: String) = {
    val params = h.drop(1)
    println("decoding from hash", Base64.getDecoder.decode(params).map(_.toChar).mkString)
    Try(read[RoutingParams](Base64.getDecoder.decode(params).map(_.toChar).mkString)).toOption
  }

  def changeViewState(s: ViewState, routingParams: RoutingParams = RoutingParams(0)) =
    routingParams.copy(stateId = s.id)
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
