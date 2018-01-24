package life.plenty.model.octopi

trait Question extends Space with WithParent[Space] {}

class BasicQuestion(override val _parent: Space, override val _title: String) extends Question {}
  //  override def preConstructor(): Unit = {
  //    super.preConstructor()
  //    //println("BasicQuestion constr", this.connections)
  //  }
//}

//object BasicQuestion extends InstantiateByApply[BasicQuestion] {
//  override def instantiate: BasicQuestion = {
//    val r = new BasicQuestion(null, null)
//    println(s"object ${this.getClass} instantiated ${r}")
//    r
//  }

  //  override def apply(className: String): Option[BasicQuestion] = {
  //    println(s"bq applying by className ${className} on ${this.getClass.getSimpleName}")
  //    val r = super.apply(className)
  //    println(s"result ${r}")
  //    r
  //  }
//}

class BasicSpace(override val _title: String) extends Space with WithMembers {

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    //    addConnection(Marker(FILL_GREAT_QUESTIONS))
    //println("basic space init", this.connections)
  }
}

//object BasicSpace extends InstantiateByApply[BasicSpace] {
//  override def instantiate: BasicSpace = {
//    val r = new BasicSpace(null)
//    println(s"object ${this.getClass} instantiated ${r}")
//    r
//  }
//}
//
//
