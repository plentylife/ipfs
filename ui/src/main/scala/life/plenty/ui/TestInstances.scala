package life.plenty.ui

import java.util.Date

import life.plenty.model.connection._
import life.plenty.model.octopi.GreatQuestions.Who
import life.plenty.model.octopi._
import life.plenty.ui.model.UiContext.getCreator
import rx.Ctx

object TestInstances {

  implicit val ctx = Ctx.Owner.safe()

  def load(): Space = {
    val ts = new BasicSpace()
    ts.asNew(Title("French" + new Date().getHours + new Date().getMinutes.toString), getCreator)

    val e = new Event
    e.asNew(Title("first event"), Parent(ts))

    ts.rx.get({ case Child(q: Who) ⇒ q }).foreach(_.foreach {
      who ⇒

        println("TestInst")

        //        who.asNew(Parent(ts), getCreator)

        val q = new BasicQuestion()
        q.asNew(Parent(who), Title("is not me"), getCreator)

        val notme = new BasicUser
        notme.asNew(Id("notme"), Name("Not Me"))
        val c = new Contribution()
        c.asNew(Parent(q), Body("not me contribution"), Creator(notme))

    })

    //    val who = new Who()

    //        val a = new BasicAnswer()
    //    a.asNew(Parent(q), Body("I am asking these"), getCreator)

    ts
  }
}

