package life.plenty.data

import life.plenty.model.ModuleRegistry
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.security.SecureUser
import life.plenty.{data, model}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main {
  def main(gunCalls: GunCalls): Future[Any] = {
    model.setHasher(DataHash)
    modules()

    data.gunCalls = gunCalls

    Future {}
  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Hub ⇒ new GunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new InstantiationGunWriterModule(o) }

    ModuleRegistry add { case o: Hub if !o.isInstanceOf[SecureUser] ⇒ new OctopusGunReaderModule(o) }
    ModuleRegistry add { case o: SecureUser ⇒ new SecureUserGunReaderModule(o) }
  }
}

