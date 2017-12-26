package life.plenty.model.utilities

import life.plenty.model.{Connection, ConnectionSet, ConnectionType, Node}

object Traversal {
  type ConSet = Set[Connection[Node]]

  /**
    * @return all connections that this node has in the given set of connections, where the node is in the `from`
    *         position */
  def getDirectFromConnections(from: Node, connectionSet: ConSet): ConSet = {
    connectionSet.filter(_.from == from)
  }

  /**
    * @return all connections that this node has in the given set of connections, where the node is in the `to`
    *         position */
  def getDirectToConnections(to: Node, connectionSet: ConSet): ConSet = {
    connectionSet.filter(_.to == to)
  }

  /**
    * @return all nodes irrespective of position that are directly connected to the given starting node with the
    *         connection type */
  def getNodesConnectedThrough(start: Node, conType: ConnectionType[_], conSet: ConSet): ConSet = {
    conSet.filter(c ⇒ inAnyPosition(start, c) && c.conType == conType)
  }
  def inAnyPosition(n: Node, c: Connection[_]): Boolean = c.to == n || c.from == n
  /**
    * @return all nodes in the from position that are directly connected to the given starting node (which is in the
    *         to position) with the connection type */
  def getNodesConnectedToThrough[T <: Node](connectedTo: Node, conType: ConnectionType[T], conSet: ConSet): Set[Node]
  = {
    conSet.filter(c ⇒ c.to == connectedTo && c.conType == conType).map(_.from)
  }

  def getNodesAtPointerIntersection(conSet: ConnectionSet): ConSet = {

  }

  def traverseBF(conSet: Connection)
}
