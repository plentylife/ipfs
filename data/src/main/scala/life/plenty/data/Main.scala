package life.plenty.data

import life.plenty.model
import life.plenty.model.ModuleRegistry
import life.plenty.model.octopi.Octopus

import scala.scalajs.js

object Main {

  private var _gun: Gun = _

  def gun: Gun = _gun

  def main(): Unit = {
    println("Data entry point")
    model.setHasher(DataHash)
    modules()

    _gun = Gun(js.Array("http://localhost:8080/gun"))
  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Octopus ⇒ new GunWriterModule(o) }
    ModuleRegistry add { case o: Octopus ⇒ new InstantiationGunWriterModule(o) }
  }
}

