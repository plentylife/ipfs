package life.plenty.ui

import life.plenty.model.NodeInstance

trait PageTrait {
  def cards: List[NodeInstance]

  def construct(cards: List[NodeInstance])
}

class Page extends PageTrait {
  private var _cards = List[NodeInstance]()
  override def construct(cards: List[NodeInstance]): Unit = {
    _cards = cards
  }
  override def cards = _cards
}
