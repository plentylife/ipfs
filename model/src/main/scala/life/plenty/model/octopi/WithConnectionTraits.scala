package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Creator, Parent}
import life.plenty.model.utils.Property

trait WithParent[T <: Octopus] extends Octopus {
  protected val _parent: T
  lazy val parent = new Property[T]({ case Parent(o: T) ⇒ o }, this, _parent)

  override def idGenerator: String = super.idGenerator + parent().id

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    parent applyInner { p ⇒
      this.addConnection(Parent(p))
      p.addConnection(Child(this))
    }
    println(s"with parent preconstructor ${this} ${parent.getSafe.map(_.connections)}", _parent, parent.getSafe)
  }
}

trait WithMembers extends Space {
  lazy val members: Property[Members] = new Property[Members]({ case Child(m: Members) ⇒ m }, this, null)

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    println("with members constructor")
  }
}

trait WithCreator extends Octopus {
  val creator: User

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    this.addConnection(Creator(creator))
  }
}

//
//trait WithId {
//
//}
