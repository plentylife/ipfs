package life.plenty.ui.model

import life.plenty.model.octopi.definition.{Hub, Module}

sealed trait ModuleOverride

sealed trait ConditionalModuleOverride extends ModuleOverride {
  val creator: DisplayModule[Hub]
  val condition: (DisplayModule[Hub]) ⇒ Boolean
}

case class ExclusiveModuleOverride(excluded: DisplayModule[_] ⇒ Boolean) extends ModuleOverride

case class SimpleModuleOverride(creator: DisplayModule[Hub], by: DisplayModule[Hub], condition:
(DisplayModule[Hub]) ⇒ Boolean) extends ConditionalModuleOverride

case class ComplexModuleOverride(creator: DisplayModule[Hub],
                                 findBy: PartialFunction[Module[Hub], DisplayModule[Hub]],
                                 condition: (DisplayModule[Hub]) ⇒ Boolean) extends ConditionalModuleOverride