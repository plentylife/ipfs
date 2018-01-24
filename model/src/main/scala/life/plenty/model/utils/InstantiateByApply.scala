package life.plenty.model.utils

trait InstantiateByApply[T] {
  def instantiate: T

  def apply(className: String): Option[T] = if (className == this.getClass.getSimpleName) Option(instantiate)
  else None
}
