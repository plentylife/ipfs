package life.plenty.data

import life.plenty.model.ModuleRegistry
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.security.SecureUser
import life.plenty.{data, model}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main {
  def main(db: ShareDB): Unit = {
    model.setHasher(DataHash)
    modules()

    data.db = db
  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Hub if !o.isInstanceOf[SecureUser] ⇒ new DbWriterModule(o) }
    ModuleRegistry add { case o: Hub if !o.isInstanceOf[SecureUser] ⇒ new DbReaderModule(o) }

    ModuleRegistry add { case o: SecureUser ⇒ new SecureUserDbReaderModule(o) }
    ModuleRegistry add { case o: SecureUser ⇒ new SecureUserDbWriterModule(o) }

//     fixme. as soon as datahubs become more sophisticated
//    ModuleRegistry add { case o: DataHub[_] ⇒ new DataHubDbWriterModule(o) }
  }
}

