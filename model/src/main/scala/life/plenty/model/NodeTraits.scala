package life.plenty.model

trait NodeType {
  val id: String
  val allowedActions: Set[ActionType]
  val mandatoryData: Set[DataType[Any]]
  val connections: Set[DefinitionConnectionType]
}

trait DefinitionConnectionType {
}

trait DefinitionConnectionInstance {
  val conType: DefinitionConnectionType
}

// there needs to be:
// questions, answers, and comments
// members

trait NodeInstance {
  val id: String
  val base: NodeType
  val connections: Set[ConnectionInstance]
  val data: Set[DataInstance[Any]]
}

trait DataType[+T] {

}

trait DataInstance[+T] {
  val dataType: DataType[T]

  def get: T
}

trait ActionType

trait ConnectionType {

}

trait ConnectionInstance {
  val conType: ConnectionType
}

// connections can be: contributed