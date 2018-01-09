package life.plenty.model.connection

trait Connection[T] {
  val value: T

  def id: String = value.hashCode().toBinaryString
}

case class Body(body: String) extends Connection[String] {
  override val value: String = body
}
