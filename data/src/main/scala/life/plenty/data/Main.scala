package life.plenty.data

import life.plenty.model.ModuleRegistry
import life.plenty.model.octopi.{BasicSpace, Octopus, Space}

import scala.scalajs.js
import scala.scalajs.js.JSON


object Main {

  private var _gun: Gun = _

  def gun = _gun

  def main(): Unit = {
    println("Data entry point")
    modules

    _gun = Gun(js.Array("http://localhost:8080/gun"))

    val ts = new BasicSpace("test")
    //    val fp = new BasicSpace("test_parent")
    //    ts.addConnection(Parent(fp))
    //

    gun.get(ts.id).`val`((d, k) ⇒ {
      println(JSON.stringify(d))
    })

    OctopusWriter.write(ts)


  }

  private def modules = {
    ModuleRegistry add { case o: Octopus ⇒ new OctopusGunReaderModule(o) }

    ModuleRegistry add { case o: Space ⇒ new SpaceConstructorWriter(o) }
  }
}

