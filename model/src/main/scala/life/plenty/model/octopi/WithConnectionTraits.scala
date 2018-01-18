package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Creator}

trait WithParent[T <: Octopus] extends Octopus {
  val parent: T
}

trait WithMembers extends Space {

  def members: Members = find getOrElse new Members(this)

  // fixme. maybe redo this
  private def find: Option[Members] = this.getTopConnectionData({ case Child(m: Members) ⇒ m })

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    if (find.isEmpty) this.addConnection(Child(new Members(this)))
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