package life.plenty.data

import life.plenty.model
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.GreatQuestions.Who
import life.plenty.model.octopi.{BasicQuestion, BasicSpace}

import scala.scalajs.js

object Main {

  private var _gun: Gun = _

  def gun = _gun

  def main(): Unit = {
    println("Data entry point")
    // fixme remove after testing
    model.setHasher(DataHash)

    _gun = Gun(js.Array("http://localhost:8080/gun"))

    val ts = new BasicSpace("test")
    val who = new Who(ts)
    val q = new BasicQuestion(who, "first question")
    val fp = new BasicSpace("test_parent")
    ts.addConnection(Parent(fp))

    println("fake data in")
    println(q, q.connections)
    println(who, who.connections)
    println(ts.id, ts.connections)

    OctopusWriter.write(ts)

    //    println(s"Trying to read ${ts.id}")
    //    OctopusReader.read(ts.id)
  }
}

