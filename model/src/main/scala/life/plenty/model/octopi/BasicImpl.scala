package life.plenty.model.octopi

import life.plenty.model.connection.Parent
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import rx.Rx

trait Question extends Space with WithParent[Space] {
  override def getTitle: Rx[Option[String]] = super.getTitle.map(_.map {t ⇒ if (t.last != '?') t+'?' else t})
}

class BasicQuestion() extends Question {}

class SignupQuestion() extends Question {}

class ContainerSpace() extends Space with WithMembers with WithOptParent[Space]

//  with CommonGetters
//
//trait CommonGetters {self: Hub ⇒
//  def getParent: Rx[Option[Hub]] = GraphUtils.getParent(self)
//}