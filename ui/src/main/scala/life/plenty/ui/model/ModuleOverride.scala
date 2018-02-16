package life.plenty.ui.model

import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.model.DisplayModel.DisplayModule

//case class ModuleOverride(creator: DisplayModule[Hub], by: DisplayModule[Hub], condition:
//(DisplayModule[Hub]) ⇒ Boolean)

case class ModuleOverride(creator: DisplayModule[Hub], by: DisplayModule[Hub], condition:
(DisplayModule[Hub]) ⇒ Boolean)