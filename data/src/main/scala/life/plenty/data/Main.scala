package life.plenty.data

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object Main {

  private var _gun: Gun = _

  def gun = _gun

  def main(): Unit = {
    println("Data entry point")
    modules

    _gun = Gun(js.Array("http://localhost:8080/gun"))

    //    val ts = new BasicSpace("test")
    //        val fp = new BasicSpace("test_parent")
    //        ts.addConnection(Parent(fp))
    //

    //    gun.get(ts.id).`val`((d, k) ⇒ {
    //      println(JSON.stringify(d))
    //    })
    //
    //    OctopusWriter.write(ts)

    OctopusReader.read("test") map {
      r ⇒ println("test", r)
    }
    println("after read")

    //    val r = Await.result(OctopusReader.read("test"), Duration.Inf)
    //          println("test", r)

  }

  private def modules = {
    //    ModuleRegistry add { case o: Octopus ⇒ new OctopusGunReaderModule(o) }

    //    ModuleRegistry add { case o: Space ⇒ new SpaceConstructorWriter(o) }
  }
}

