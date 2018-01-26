package life.plenty.model.octopi

//val _sizeAndDirection: Int, val answer: Answer, val by: User, override val _basicInfo: BasicInfo
class Vote() extends
  Octopus {

  lazy val sizeAndDirection = null
  //  lazy val sizeAndDirection = new Property[Int]({ case Amount(a) ⇒ a }, this, _sizeAndDirection)

  override protected def preConstructor() = {
    super.preConstructor()
    //    println("new vote for user", by, by.id, by.connections)
    //    addConnection(Parent(answer))
    //    addConnection(Parent(by))
    //    addConnection(Amount(_sizeAndDirection))
    //    answer.addConnection(Child(this))
    //    by.addConnection(Child(this))
  }
}

//val size: Int, val owner: User, override val _basicInfo: BasicInfo
class VoteAllowance() extends Octopus {

  override protected def preConstructor() = {
    //    super.preConstructor()
    //    addConnection(Parent(owner))
    //    addConnection(Amount(size))
    //    owner.addConnection(Child(this))
  }
}