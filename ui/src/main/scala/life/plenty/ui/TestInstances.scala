package life.plenty.ui

import life.plenty.model._
import life.plenty.model.predef.CardDefinitionInstance

object TestInstances {
  val instances = List(

  )

  val commentDef = CardInstance(CardDefinitionInstance, properties = Set(
    PropertyInstance(CardBase, Text),
    PropertyInstance(AllowedChildren, commentDef)
  ))


}

