package life.plenty.model.utils

trait InstantiateByApply[T] {
  def instantiate: T

  def apply(className: String): Option[T] = {
    println(s"applying by className ${className} on ${this.getClass.getSimpleName}")
    val r = if (className == this.getClass.getSimpleName) Option(instantiate) else None
    println(s"result ${r}")
    r
  }
}
