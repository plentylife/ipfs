package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}
import rx.Rx

trait WithParent[T <: Octopus] extends Octopus {
  override def required = super.required + getParent

  def getParent: Rx[Option[Octopus]] = rx.get({ case Parent(p: Octopus) ⇒ p })

  override def idGenerator: String = super.idGenerator + getParent.now.get.id

  onInstantiate {
    getParent.foreach(_.foreach { p: Octopus ⇒
      println(s"adding child to parent from ${this} to $p")
      p.addConnection(Child(this).inst)
    })
  }
}

trait WithMembers extends Space {
  //  def members: Members = new Property[Members]({ case Child(m: Members) ⇒ m }, this, null)
}