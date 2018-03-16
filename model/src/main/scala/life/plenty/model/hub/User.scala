package life.plenty.model.hub

import life.plenty.model
import life.plenty.model.connection.{Child, Email, Name}
import life.plenty.model.hub.definition.Hub
import rx.Rx

trait User extends Hub {
  clearRequired()
  addToRequired(getRxId)
  addToRequired(getName)
  addToRequired(getEmail)

  lazy val getTransactions = rx.getAll({ case Child(t: Transaction) ⇒ t })
  lazy val getTransactionsTo = filterTransactions(getTransactions, t ⇒ t.getTo)
  lazy val getTransactionsFrom = filterTransactions(getTransactions, t ⇒ t.getFrom)
  lazy val getVoteAllowances: Rx[List[VoteAllowance]] = rx.getAll({ case Child(a: VoteAllowance) ⇒ a })
  lazy val getVotes = rx.getAll({ case Child(v: Vote) ⇒ v })

  private def filterTransactions(rxList: Rx[List[Transaction]], field: (Transaction) ⇒ Rx[Option[User]])
  : Rx[List[Transaction]] = {
    rxList.map({ list: List[Transaction] ⇒
      model.console.trace(s"User filterTransaction() ${list} ${this}")
      list.flatMap({ t: Transaction ⇒
        model.console.trace(s"User filterTransaction single ${list} ${this}")
        field(t)().collect({ case u: User if (u.id == this.id) ⇒ t }): Option[Transaction]
      })
    })
  }

  override def generateId: String = {
    throw new NotImplementedError(s"this method not supposed to be used for users. Connections ${_connections}")
  }

  def getName: Rx[Option[String]] = rx.get({ case Name(n: String) ⇒ n })
  def getEmail: Rx[Option[String]] = rx.get({ case Email(n: String) ⇒ n })

  def getNameOrEmpty: Rx[String] = getName.map(_.getOrElse(""))

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser() extends User