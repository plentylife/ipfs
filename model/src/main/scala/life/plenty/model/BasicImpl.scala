package life.plenty.model

class BasicQuestion(override val parent: Space, override val title: String) extends Question {
  override def preConstructor(): Unit = {
    super.preConstructor()
    println("BasicQuestion constr", this.connections)
  }
}
