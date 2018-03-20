package life.plenty.model.utils

import life.plenty.model.connection.{Body, Marker, MarkerEnum}
import life.plenty.model.hub.definition.{Hub, Insert, Remove}
import life.plenty.model.utils.DeprecatedGraphExtractors.confirmedMarker
import monix.reactive.Observable
import rx.{Ctx, Rx}

object GraphExtractors {

  def isMarkedConfirmed(h: Hub): Observable[Boolean] =
    h.getStream({case Marker(MarkerEnum.CONFIRMED) ⇒ true}).asBoolean

  def getBody(h: Hub) = h.getInsertStream.collect({ case Body(b) ⇒ b })

  def isMarkedContributing(h: Hub): Observable[Boolean] =
    h.getStream({ case c@Marker(m) if m == MarkerEnum.CONTRIBUTING_QUESTION ⇒ c }).asBoolean

}
