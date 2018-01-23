package life.plenty.data

import life.plenty.model
import life.plenty.model.octopi.BasicSpace

import scala.scalajs.js

object Main {

  private var _gun: Gun = _

  def gun = _gun

  def main(): Unit = {
    println("Data entry point")
    // fixme remove after testing
    model.setHasher(DataHash)

    modules

    _gun = Gun(js.Array("http://localhost:8080/gun"))

    val ts = new BasicSpace("test")
    //        val fp = new BasicSpace("test_parent")
    //        ts.addConnection(Parent(fp))
    //

    //    gun.get(ts.id).`val`((d, k) ⇒ {
    //      println(JSON.stringify(d))
    //    })
    //
    //      println("writing")
    //            OctopusWriter.write(ts)

    println(s"Trying to read ${ts.id}")
    OctopusReader.read(ts.id)

    //    val r = Await.result(OctopusReader.read("test"), Duration.Inf)
    //          println("test", r)

  }

  private def modules = {
    //    ModuleRegistry add { case o: Octopus ⇒ new OctopusGunReaderModule(o) }

    //    ModuleRegistry add { case o: Space ⇒ new SpaceConstructorWriter(o) }
  }
}

