package life.plenty.data

import life.plenty.model.connection.Parent
import life.plenty.model.octopi.BasicSpace

import scala.scalajs.js


object Main {

  private var _gun: Gun = _

  def gun = _gun

  def main(): Unit = {
    println("Data entry point")

    _gun = Gun(js.Array("http://localhost:8080/gun"))



    //    gun.get("constructor-args")
    //        .put(new ConstructorArgs {
    //          override val parent: String = "parent-id"
    //          override val id: Int = 123
    //        }, (ack) ⇒ {
    //          println(JSON.stringify(ack))
    //        }).on((d,k) ⇒ {
    //      println(JSON.stringify(d))
    //      println(d.asInstanceOf[ConstructorArgs].id)
    //    })

    val ts = new BasicSpace("test")
    val fp = new BasicSpace("test_parent")
    ts.addConnection(Parent(fp))

    OctopusWriter.write(ts)

    OctopusReader.read("test")
  }

  private def modules = {

  }
}

//@ScalaJSDefined
//trait ConstructorArgs extends js.Object {
//  val parent: String
//  val id: Int
//}
