package life.plenty.data

import life.plenty.model
import life.plenty.model.ModuleRegistry
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.GreatQuestions.Who
import life.plenty.model.octopi.{BasicAnswer, BasicQuestion, BasicSpace, Octopus}

import scala.scalajs.js

object Main {

  private var _gun: Gun = _

  def gun: Gun = _gun

  def main(): Unit = {
    println("Data entry point")
    // fixme remove after testing
    model.setHasher(DataHash)
    modules()

    _gun = Gun(js.Array("http://localhost:8080/gun"))

    val ts = new BasicSpace("test")
    val who = new Who(ts)
    val q = new BasicQuestion(who, "is asking these")
    val a = new BasicAnswer(q, "I am asking these")
    val fp = new BasicSpace("test_parent")
    ts.addConnection(Parent(fp))

    //    println("fake data in")
    //    println(q, q.id, q.connections)
    //    println(who, who.connections)
    //    println(ts.id, ts.connections)

    OctopusWriter.write(ts)
    OctopusWriter.write(who)
    OctopusWriter.write(q)
    OctopusWriter.write(a)

    //    println(s"Trying to read ${ts.id}")
    //    OctopusReader.read(ts.id)
  }

  def modules(): Unit = {
    ModuleRegistry add { case o: Octopus ⇒ new GunWriterModule(o) }
  }
}

