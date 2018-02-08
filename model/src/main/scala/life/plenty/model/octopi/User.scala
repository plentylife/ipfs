package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection.{Child, Name}
import life.plenty.model.octopi.definition.Octopus
import rx.Rx

trait User extends Octopus {
  clearRequired()
  addToRequired(getRxId)
  addToRequired(getName)

  lazy val getTransactions = rx.getAll({ case Child(t: Transaction) ⇒ t })
  lazy val getTransactionsTo = filterTransactions(getTransactions, t ⇒ t.getTo)
  lazy val getTransactionsFrom = filterTransactions(getTransactions, t ⇒ t.getFrom)
  lazy val getVoteAllowances: Rx[List[VoteAllowance]] = rx.getAll({ case Child(a: VoteAllowance) ⇒ a })
  lazy val getVotes = rx.getAll({ case Child(v: Vote) ⇒ v })

  private def filterTransactions(rxList: Rx[List[Transaction]], field: (Transaction) ⇒ Rx[Option[User]])
  : Rx[List[Transaction]] = {
    rxList.map({ list: List[Transaction] ⇒
      model.console.trace(s"User filterTransaction() ${list}")
      list.flatMap({ t: Transaction ⇒
        field(t)().collect({ case u: User if (u.id == this.id) ⇒ t }): Option[Transaction]
      })
    })
  }

  override def generateId: String = {
    throw new NotImplementedError(s"this method not supposed to be used for users. Connections ${_connections.now}")
  }

  def getName: Rx[Option[String]] = rx.get({ case Name(n: String) ⇒ n })

  def getNameOrEmpty: Rx[String] = getName.map(_.getOrElse(""))

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser() extends User {

}