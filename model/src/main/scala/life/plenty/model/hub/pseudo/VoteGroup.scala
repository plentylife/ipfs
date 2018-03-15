package life.plenty.model.hub.pseudo

import life.plenty.model.connection.{Created, Parent}
import life.plenty.model.hub.{Answer, Vote}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphExtractors._
import rx.{Ctx, Rx}

import scala.collection.parallel.IterableSplitter
import scala.language.postfixOps

case class VoteGroup(votes: Iterable[Vote], created: Long) extends CreatedProperty

object VoteGroup {
  def groupByAnswer(list: Iterable[Hub])(implicit ctx: Ctx.Owner): Rx[Iterable[VoteGroup]] = Rx {
    val withParent = list collect {case v: Vote ⇒ v} map {h ⇒
      val p = h.parentAnswer.filter(_.nonEmpty)
      h → p()
    }
    // so we have to group by answer and creator
    withParent.groupBy(_._2).filter(_._1.nonEmpty) flatMap {g ⇒
      val vg: Iterable[(Vote, Option[Answer])] = g._2
      val vs: Iterable[Vote] = vg map {v ⇒ v._1}
      VoteGroup(vs)
    }
  }

  def apply(votes: Iterable[Vote])(implicit ctx: Ctx.Data): Option[VoteGroup] = {
    val created = votes map {v ⇒
      val t = getCreationTime(v).filter(_.nonEmpty)
      t()
    } collect {case Some(t) ⇒ t}
    created.headOption map {
      _ ⇒ VoteGroup(votes, created.min)
    }
  }
}