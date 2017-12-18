package life.plenty.model

case class Card[T <: CardBase](
  base: T,
  properties: Set[PropertyInstance[Any]]
                       )

case class PropertyInstance[T](property: CardProperty[T], value: T)