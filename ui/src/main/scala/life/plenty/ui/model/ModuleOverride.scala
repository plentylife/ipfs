package life.plenty.ui.model

import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui.model.DisplayModel.DisplayModule

sealed trait ModuleOverride {
  val creator: DisplayModule[Hub]

  val condition: (DisplayModule[Hub]) ⇒ Boolean
}

case class SimpleModuleOverride(creator: DisplayModule[Hub], by: DisplayModule[Hub], condition:
(DisplayModule[Hub]) ⇒ Boolean) extends ModuleOverride

case class ComplexModuleOverride(creator: DisplayModule[Hub],
                                 findBy: PartialFunction[Module[Hub], DisplayModule[_]],
                                 condition: (DisplayModule[Hub]) ⇒ Boolean) extends ModuleOverride