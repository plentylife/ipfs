package life.plenty.model.connection

trait Connection[T] {
  def value: T

  def id: String = value.hashCode().toBinaryString
}
