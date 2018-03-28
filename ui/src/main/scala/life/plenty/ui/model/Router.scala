package life.plenty.ui.model

import java.util.Base64

import com.thoughtworks.binding.Route
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.model.ViewState.ViewState
import org.scalajs.dom.window
import upickle.default.{macroRW, ReadWriter ⇒ RW, _}

import scala.util.Try

object Router {
  lazy val router: Route.Hash[RoutingParams] = Route.Hash(defaultRoutingParams)(new Route.Format[RoutingParams] {
    override def unapply(hashText: String): Option[RoutingParams] = {
      fromHash(hashText)
    }

    override def apply(state: RoutingParams): String = toHash(state)
  })

  def defaultRoutingParams = fromHash(window.location.hash) getOrElse
    changeViewState(ViewState.DISCUSSION, RoutingParams(0, None))

  private var lastParams: Option[RoutingParams] = None
  def initialize = {
    lastParams = fromHash(window.location.hash)
    println(s"Router has params ${lastParams}")
    router.watch()
  }

  def reRender(o: Hub, p: RoutingParams) = {
    //    println("routing params (current, last)", p, lastParams.orNull)
    if (p != lastParams.orNull) {
      //      println("Router re-render")
      lastParams = Option(p)
      DisplayModel.reRender(o)
    }
  }

  def toHash(r: RoutingParams): String = {
    "#" + Base64.getUrlEncoder.encodeToString(write(r).getBytes)
  }
  def fromHash(h: String) = {
    val params = h.drop(1)
    //    println("decoding from hash", Base64.getDecoder.decode(params).map(_.toChar).mkString)
    Try(read[RoutingParams](Base64.getUrlDecoder.decode(params).map(_.toChar).mkString)).toOption
  }

  def changeViewState(s: ViewState, routingParams: RoutingParams) =
    routingParams.copy(stateId = s.id)

  def changeHub(hub: Hub, routingParams: RoutingParams) =
    routingParams.copy(hubId = Option(hub.id))

  def getHubLink(hub: Hub) = {
    toHash(changeHub(hub, router.state.value))
  }

  def navigateToHub(o: Hub) = {
    router.state.value_=(router.state.value.copy(hubId = Option(o.id)))
  }
}

object ViewState extends Enumeration {
  type ViewState = Value
  val DISCUSSION, RATING = Value
}

case class RoutingParams(stateId: Int, hubId: Option[String]) {
  def state = ViewState(stateId)
}

object RoutingParams {
  //  implicit val viewStateWriter: Writer[ViewState] = Writer(vs ⇒ writeJs(vs.id) )
  implicit def rw: RW[RoutingParams] = macroRW
}
