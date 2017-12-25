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

class ConnectionSet(val startingSet: Set[Connection[Node]], startingNode: Node) {
  private var _pointers: Set[Node] = Set(startingNode)
  private var _connections: Set[Connection[Node]] = startingSet

  def pointers = _pointers
  def addPointer(p: Node) = {_pointers += p}
  def removePointer(p: Node) = {_pointers -= p}

  def connections = _connections
  def addConnection(c: Connection[Node]) = _connections += c
}