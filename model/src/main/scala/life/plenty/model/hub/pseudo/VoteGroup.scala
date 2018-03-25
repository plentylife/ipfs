package life.plenty.model.hub.pseudo

import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.{Answer, User, Vote}
import life.plenty.model.utils.GraphEx._
import rx.{Ctx, Rx}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.language.postfixOps

case class VoteGroup(created: Long, answer: Answer, votes: List[Vote])

object VoteGroup {
  def groupByAnswer(list: List[Hub]): Future[List[VoteGroup]] = {
    val withParent = list collect { case v: Vote ⇒ v } map { h ⇒
      val p = h.parentAnswer.filter(_.nonEmpty)
      p.map(h → _)
    }
    // so we have to group by answer and creator
    Future.sequence(withParent).flatMap { ws ⇒
      val packed = ws.groupBy(_._2).filter(_._1.nonEmpty) map { g ⇒
        val vg: List[(Vote, Option[Answer])] = g._2
        val vs: List[Vote] = vg map { v ⇒ v._1 }
        val creationTime = getCreationTime(g._1.get)
        for (t ← creationTime) yield VoteGroup(t.get, g._1.get, vs)
      } toList;
      Future.sequence(packed)
    }
  }

  def countByUser(list: List[Vote]): Future[List[(User, Int)]] = {
    val grouped = Future.sequence(list map (v ⇒ getCreator(v) map {_ → v})) map {
      _ groupBy(_._1)
    }
    val userAmounts = grouped flatMap {grouped ⇒

      val fs = grouped.collect { case (Some(u), lv) ⇒ u -> lv.map{_._2} } map {
        case (u, lv) ⇒
          val voteAmounts = Future.sequence(lv map { v ⇒ v.getAmountOrZero })
          voteAmounts map {vas ⇒ u -> (0 :: vas).sum}
      }
      Future.sequence(fs)
    }

    userAmounts map {_.toList}
  }
}