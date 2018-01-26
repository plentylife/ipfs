package life.plenty.model.octopi

trait Question extends Space with WithParent[Space] {}

class BasicQuestion(override val _parent: Space, override val _title: String, override val _basicInfo: BasicInfo)
  extends Question {}

class BasicSpace(override val _title: String, override val _basicInfo: BasicInfo) extends Space with WithMembers {

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    //    addConnection(Marker(FILL_GREAT_QUESTIONS))
    //println("basic space init", this.connections)
  }
}