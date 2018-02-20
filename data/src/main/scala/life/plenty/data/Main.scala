package life.plenty.data

import life.plenty.model
import life.plenty.model.ModuleRegistry
import life.plenty.model.octopi.definition.Hub

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

object Main {
  def gun: Gun = GunCalls.getInstance()

  def main(bootstrapPeers: js.Array[String]): Future[Any] = {
    println(s"Data entry point with peers ${bootstrapPeers.toList}")
    model.setHasher(DataHash)
    modules()

//    val peersOpt = js.Dynamic.literal()
//    bootstrapPeers.foreach(p ⇒ {
//      peersOpt.updateDynamic(p)("")
//    })
    GunCalls.instantiate(bootstrapPeers).toFuture
  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Hub ⇒ new GunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new InstantiationGunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new OctopusGunReaderModule(o) }
  }
}

