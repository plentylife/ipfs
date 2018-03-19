package life.plenty.model.hub.pseudo

import life.plenty.model.hub.{Answer, User, Vote}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.DeprecatedGraphExtractors._
import life.plenty.model.utils.GraphUtils
import rx.{Ctx, Rx}

import scala.language.postfixOps

case class VoteGroup(created: Long, answer: Answer, votes: List[Vote])

object VoteGroup {
  def groupByAnswer(list: List[Hub])(implicit ctx: Ctx.Owner): Rx[List[VoteGroup]] = Rx {
    val withParent = list collect {case v: Vote ⇒ v} map {h ⇒
      val p = h.parentAnswer.filter(_.nonEmpty)
      h → p()
    }
    // so we have to group by answer and creator
    withParent.groupBy(_._2).filter(_._1.nonEmpty) flatMap {g ⇒
      val vg: List[(Vote, Option[Answer])] = g._2
      val vs: List[Vote] = vg map {v ⇒ v._1}
      VoteGroup.pack(vs)
    } toList
  }

  def countByUser(list: List[Vote])(implicit ctx: Ctx.Owner): Rx[Map[User, Int]] = Rx {
    list groupBy(v ⇒ v.getCreator()) collect {case (Some(u), lv) ⇒ u -> lv} map {
      case (u, lv) ⇒
        val voteAmounts = lv map {v ⇒ v.getAmountOrZero()}
        u -> (0 :: voteAmounts).sum
    }
  }

  def pack(votes: List[Vote])(implicit ctx: Ctx.Owner, cd: Ctx.Data): Option[VoteGroup] = {
    val created = votes map {v ⇒
      val t = getCreationTime(v).filter(_.nonEmpty)
      t()
    } collect {case Some(t) ⇒ t}
    votes.headOption flatMap  {
          _.parentAnswer() map {
            p ⇒ VoteGroup(created.min, p, votes)
          }
    }
  }
}