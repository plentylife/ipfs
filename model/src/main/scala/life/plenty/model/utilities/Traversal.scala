package life.plenty.model.utilities

import life.plenty.model.{Connection, Node}

object Traversal {
  /**
    * @return all connections that this node has in the given set of connections, where the node is in the `from`
    *         position */
  def getDirectFromConnections(from: Node, connectionSet: Set[Connection[_]]): Set[Connection[_]] = {
    connectionSet.filter(_.from == from)
  }

  /**
    * @return all connections that this node has in the given set of connections, where the node is in the `to`
    *         position */
  def getDirectToConnections(to: Node, connectionSet: Set[Connection[_]]): Set[Connection[_]] = {
    connectionSet.filter(_.to == to)
  }
}
