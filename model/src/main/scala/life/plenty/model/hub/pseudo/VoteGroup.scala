package life.plenty.model.hub.pseudo

import life.plenty.model.hub.{Answer, Vote}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphExtractors._
import rx.{Ctx, Rx}

import scala.language.postfixOps

case class VoteGroup(created: Long, votes: List[Vote])

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

  def pack(votes: List[Vote])(implicit ctx: Ctx.Owner, cd: Ctx.Data): Option[VoteGroup] = {
    val created = votes map {v ⇒
      val t = getCreationTime(v).filter(_.nonEmpty)
      t()
    } collect {case Some(t) ⇒ t}
    created.headOption map {
      _ ⇒ VoteGroup(created.min, votes)
    }
  }
}