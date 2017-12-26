package life.plenty.model

trait ConnectionType[+F <: Node]

trait NodeConnectionType extends ConnectionType[Node]

trait DataConnectionType extends ConnectionType[Data[Any]]

object INSTANCE_OF extends NodeConnectionType

object INSIDE_SPACE_OF extends NodeConnectionType

//object STARTING_POINT extends ConnectionType
object ALLOWED_CHILD extends NodeConnectionType

object DISALLOWED_CHILD extends NodeConnectionType

object USER_CREATED_INSTANCE_OF extends NodeConnectionType


case class Connection[+F <: Node](from: F, conType: ConnectionType[F], to: Node)

case class ConnectionSet(connections: Set[Connection[Node]], pointers: Set[Node]) {
  def addPointer(p: Node) = {this.copy(pointers = pointers + p)}
  def removePointer(p: Node) = {this.copy(pointers = pointers - p)}

  def addConnection(c: Connection[Node]) = this.copy(connections = connections + c)
}