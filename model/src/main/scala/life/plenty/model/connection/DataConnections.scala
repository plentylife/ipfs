package life.plenty.model.connection

import scala.util.Try

case class Title(title: String) extends Connection[String] {
  override def value: String = title
}

object Title extends InstantiateFromStringByApply[Title] {
  override def instantiate(from: String): Option[Title] = Option(Title(from))
}

case class Body(body: String) extends Connection[String] {
  override def value: String = body
}

object Body extends InstantiateFromStringByApply[Body] {
  override def instantiate(from: String): Option[Body] = Option(Body(from))
}

case class Amount(amount: Int) extends Connection[Int] {
  override def value: Int = amount
}

object Amount extends InstantiateFromStringByApply[Amount] {
  override def instantiate(from: String): Option[Amount] = Try(from.toInt).toOption map { a ⇒ Amount(a) }
}

case class Id(idValue: String) extends Connection[String] {
  override def value: String = idValue
}

object Id extends InstantiateFromStringByApply[Id] {
  override def instantiate(from: String): Option[Id] = Option(Id(from))
}

case class Name(name: String) extends Connection[String] {
  override def value: String = name
}

object Name extends InstantiateFromStringByApply[Name] {
  override def instantiate(from: String): Option[Name] = Option(Name(from))
}

case class CreationTime(time: Long) extends Connection[Long] {
  override def value: Long = time
}

object CreationTime extends InstantiateFromStringByApply[CreationTime] {
  override def instantiate(from: String): Option[CreationTime] = Try(from.toLong).toOption map { a ⇒ CreationTime(a) }
}

