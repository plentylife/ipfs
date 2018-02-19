package life.plenty.data

import life.plenty.model
import life.plenty.model.ModuleRegistry
import life.plenty.model.octopi.definition.Hub

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

object Main {

  private var _gun: Gun = _

  def gun: Gun = _gun

  def main(bootstrapPeers: js.Array[String]): Future[Unit] = {
    println(s"Data entry point with peers ${bootstrapPeers.toList}")
    model.setHasher(DataHash)
    modules()

    LevelDB.open().toFuture.map(db ⇒ {
      val peersOpt = js.Dynamic.literal()
      bootstrapPeers.foreach(p ⇒ {
        peersOpt.updateDynamic(p)("")
      })
      val config = new GunConfig
      config.level = db
      config.localStorage = false
      config.peers = peersOpt
      config.file = false

      _gun = GunConstructor(config)
    })

  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Hub ⇒ new GunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new InstantiationGunWriterModule(o) }
    ModuleRegistry add { case o: Hub ⇒ new OctopusGunReaderModule(o) }
  }
}

