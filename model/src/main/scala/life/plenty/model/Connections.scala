package life.plenty.model

trait ConnectionType[+T <: Node]

trait NodeConnectionType extends ConnectionType[Node]

trait DataConnectionType extends ConnectionType[Data[Any]]

object INSTANCE_OF extends NodeConnectionType

object INSIDE_SPACE_OF extends NodeConnectionType

//object STARTING_POINT extends ConnectionType
object ALLOWED_CHILD extends NodeConnectionType

object DISALLOWED_CHILD extends NodeConnectionType

object USER_CREATED_INSTANCE_OF extends NodeConnectionType


case class Connection[+T <: Node](from: T, conType: ConnectionType[T], to: T)