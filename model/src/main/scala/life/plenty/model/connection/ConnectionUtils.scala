package life.plenty.model.connection

import scala.util.Try

object ConnectionsUtils {
  def strToLong[T](from: String, f: Long ⇒ T): Option[T] =
    Try(from.toLong).toOption map { a ⇒ f(a) }
}