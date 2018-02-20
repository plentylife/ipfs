package life.plenty.data

import life.plenty.{data, model}
import life.plenty.model.ModuleRegistry
import life.plenty.model.octopi.definition.Hub

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

object Main {
  def main(gunCalls: GunCalls): Future[Any] = {
    model.setHasher(DataHash)
    modules()

    data.gunCalls = gunCalls

    Future{}
  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Hub ⇒ new GunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new InstantiationGunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new OctopusGunReaderModule(o) }
  }
}

