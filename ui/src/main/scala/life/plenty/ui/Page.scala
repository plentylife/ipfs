package life.plenty.ui

import life.plenty.model.CardInstance

trait PageTrait {
  def cards: List[CardInstance]
  def construct(cards: List[CardInstance])
}

class Page extends PageTrait {
  private var _cards = List[CardInstance]()
  override def construct(cards: List[CardInstance]): Unit = {
    _cards = cards
  }
  override def cards = _cards
}
