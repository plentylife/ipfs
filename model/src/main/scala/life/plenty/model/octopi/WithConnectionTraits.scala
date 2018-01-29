package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection.{Amount, Child, Parent}
import rx.Rx

trait WithParent[T <: Octopus] extends Octopus {
  addToRequired(getParent)

  def getParent: Rx[Option[Octopus]] = rx.get({ case Parent(p: Octopus) ⇒ p })

  //  override def idGenerator: String = super.idGenerator + getParent.now.get.id

  onNew {
    getParent.foreach(_.foreach { p: Octopus ⇒
      model.console.println(s"adding child to parent from ${this} to $p")
      p.addConnection(Child(this))
    })
  }
}

trait WithAmount extends Octopus {
  addToRequired(getAmount)

  def getAmount = rx.get({ case Amount(a) ⇒ a })
}

trait WithMembers extends Space {
  lazy val members = rx.get({ case Child(m: Members) ⇒ m })

  onNew {
    val m = new Members
    m.asNew(Parent(this))
  }
}