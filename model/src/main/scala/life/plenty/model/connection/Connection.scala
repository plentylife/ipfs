package life.plenty.model.connection

trait Connection[T] {
  val value: T

  def id: String = value.hashCode().toBinaryString
}
