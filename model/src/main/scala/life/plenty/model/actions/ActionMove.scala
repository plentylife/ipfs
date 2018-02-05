package life.plenty.model.actions

import life.plenty.model.connection.{Child, Parent, Removed}
import life.plenty.model.octopi.{Module, Octopus}
import rx.Ctx

class ActionMove(override val withinOctopus: Octopus) extends Module[Octopus] {
  // todo. check if this is ok RX CTX
  private implicit val ctx = Ctx.Owner.Unsafe

  def moveParent(newParent: Octopus) = {
    val currentParent = withinOctopus.rx.get({ case p: Parent[_] ⇒ p })
    currentParent.foreach(_.foreach { pCon ⇒
      currentParent.kill()
      withinOctopus.addConnection(Removed(pCon.id))
      val child = pCon.parent.rx.get({ case c@Child(o: Octopus) if o == withinOctopus ⇒ c })
      child.foreach(_.foreach { cCon ⇒
        child.kill()
        pCon.parent.addConnection(Removed(cCon.id))
      })
    })

    newParent.addConnection(Child(withinOctopus))
    withinOctopus.addConnection(Parent(newParent))
  }
}
