package life.plenty.model

object Text extends CardBase {
  override val cardProperties = List(TextContentProperty)
}


//object Comment extends Text {
//  override val allowedChildrenTypes = Right(List(Comment))
//}

//trait CardGroup extends CardBase {
//  override val cardProperties = List(DescriptionProperty)
//}
//object CardGroup extends CardGroup
//
//trait CardDefinitionInstance extends CardBase {
//  override val cardProperties = List(CardBase)
//}
//object CardDefinitionInstance extends CardBase

//object Member(member: Member) extends CardDefinition
//object SignUp()