package life.plenty.model.hub

import life.plenty.model
import life.plenty.model.connection.{Child, Parent, RootParent}
import life.plenty.model.utils._
import scala.concurrent.ExecutionContext.Implicits.global

class Vote() extends WithAmount {

  @deprecated
  lazy val sizeAndDirection = getAmountRx
  @deprecated
  lazy val parentAnswerRx = rx.get({ case Parent(a: Answer) ⇒ a })
  lazy val parentAnswer = lc.ex({ case Parent(a: Answer) ⇒ a })

  addToRequired(sizeAndDirection)
  addToRequired(parentAnswerRx)

  onNew {
    // should not be empty
    creator.get.addConnection(Child(this)) foreach {_ ⇒
      parentAnswer.get.addConnection(Child(this))
      model.console.trace(s"New vote added as a child to ${parentAnswer} | ${parentAnswer.get.lc.lazyAll}")
    }

  }
}

class VoteAllowance() extends WithAmount {

  lazy val onTransaction = rx.get({ case Parent(t: Transaction) ⇒ t })

  addToRequired(onTransaction)

  onNew {
    onTransaction.foreach(_.foreach { thisTransaction ⇒
      model.console.trace(s"VoteAllowance on ${thisTransaction} ${thisTransaction.id}")
      // have to check that From user has this transaction added
      thisTransaction.getFrom.foreach(_.foreach { from ⇒
        model.console.trace(s"VoteAllowance from $from ${from.id}")

        val existing = from.rx.get({ case Child(t: Transaction) if t.id == thisTransaction.id ⇒ t })

        existing.forEach(t ⇒ {
          from.addConnection(Child(this))
          val rp = DeprecatedGraphExtractors.getRootParentOrSelf(t).now
          this.addConnection(RootParent(rp))

          model.console.trace(s"VoteAllowance given to user ${from.id} ${t.id} | root p ${rp}")

          existing.kill()
        })
      })
    })

    onTransaction.addConnection(Child(this))

  }
}