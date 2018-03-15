package life.plenty.model.connection

import life.plenty.model.hub.definition.Hub

import scala.util.Try

case class Title(title: String) extends DataHub[String] {
  override def value: String = title
}

object Title extends InstantiateFromStringByApply[Title] {
  override def instantiate(from: String): Option[Title] = Option(Title(from))
}

case class Body(body: String) extends DataHub[String] {
  override def value: String = body
}

object Body extends InstantiateFromStringByApply[Body] {
  override def instantiate(from: String): Option[Body] = Option(Body(from))
}

case class Amount(amount: Int) extends DataHub[Int] {
  override def value: Int = amount
}

object Amount extends InstantiateFromStringByApply[Amount] {
  override def instantiate(from: String): Option[Amount] = Try(from.toInt).toOption map { a ⇒ Amount(a) }
}

case class Id(idValue: String) extends DataHub[String] {
  // special case, because if id requered id of parent, it would cause infinite recursion
  override def setHolder(hub: Hub): Unit = Unit

  override val idLength: Int = 9

  override def value: String = idValue
}

object Id extends InstantiateFromStringByApply[Id] {
  override def instantiate(from: String): Option[Id] = Option(Id(from))
}

case class Name(name: String) extends DataHub[String] {
  override def value: String = name
}

object Name extends InstantiateFromStringByApply[Name] {
  override def instantiate(from: String): Option[Name] = Option(Name(from))
}

case class Email(name: String) extends DataHub[String] {
  override def value: String = name
}

object Email extends InstantiateFromStringByApply[Email] {
  override def instantiate(from: String): Option[Email] = Option(Email(from))
}

case class CreationTime(time: Long) extends DataHub[Long] {
  override def value: Long = time
}

object CreationTime extends InstantiateFromStringByApply[CreationTime] {
  override def instantiate(from: String): Option[CreationTime] = ConnectionsUtils.strToLong(from, CreationTime(_))
}