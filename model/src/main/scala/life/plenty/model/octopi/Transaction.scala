package life.plenty.model.octopi

// val amount: Int, val from: User, val on: Contribution, override val _basicInfo: BasicInfo
class Transaction() extends
  Octopus {
  override protected def preConstructor(): Unit = {
    //    super.preConstructor()
    //    addConnection(Amount(amount))
    //    addConnection(From(from))
    //    addConnection(To(on.creator()))
    //    println("adding transaction to")
    //    addConnection(Parent(on))
    //    println("--- adding transaction")
    //    from.addConnection(Child(this))
    //    on.creator().addConnection(Child(this))
    //    on.addConnection(Child(this))
  }
}
