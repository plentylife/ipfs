package life.plenty.model.octopi

import life.plenty.model.connection.Child

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
