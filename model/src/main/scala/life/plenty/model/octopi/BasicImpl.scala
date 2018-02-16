package life.plenty.model.octopi

import rx.Rx

trait Question extends Space with WithParent[Space] {
  override def getTitle: Rx[Option[String]] = super.getTitle.map(_.map {t ⇒ if (t.last != '?') t+'?' else t})
}

class BasicQuestion() extends Question {}

class SignupQuestion() extends Question {}

class ContainerSpace() extends Space with WithMembers with WithParent[Space] {
}