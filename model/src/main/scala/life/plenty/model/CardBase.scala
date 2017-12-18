package life.plenty.model

trait CardBase {

  val id: String
  val parent: Option[CardBase]
  /**
    * @note Left means all children types are allowed, an empty List means that no child is allowed
    * */
  val allowedChildrenTypes: Either[Unit, List[CardBase]] = Left()
  val mandatoryChildrenTypes: List[CardBase] = List()

  val cardProperties: List[CardProperty[Any]] = List()
  val baseProperties: List[CardBaseProperty[Any]] = List()
}

trait CardBaseProperty[+T] {
  val value: T
}

trait CardProperty[+T] {
  val mandatory: Boolean
}

case class DescriptionProperty(value: String) extends CardBaseProperty[String]
case class NameProperty(value: String) extends CardBaseProperty[String]
case class BelongsToGroup(value: CardBase) extends CardBaseProperty[CardBase]


trait TextContentProperty extends CardProperty[String]
//trait AllowedChildren extends CardProperty[CardDefinitionInstance]