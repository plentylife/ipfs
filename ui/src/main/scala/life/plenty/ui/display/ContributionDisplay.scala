package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionTip
import life.plenty.model.octopi.Contribution
import life.plenty.ui
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.Event
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.Node
import rx.Ctx

import scala.util.{Failure, Success, Try}

class ContributionDisplay(override val withinOctopus: Contribution) extends DisplayModule[Contribution] {
  //  protected val body = Var[String](withinOctopus.body())
  private val tipsCollected = Var(0)
  private val open = Var(false)
  private var tipping: Int = 1
  private var error = Var("")

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()


  override def update(): Unit = {
    tipsCollected.value_=(withinOctopus.countTips)
    //    body.value_=(withinOctopus._body)
  }

  @dom
  override def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    val disabled = findTipModule.isEmpty
    <div class="card d-inline-flex mt-1 mr-1 flex-row contribution">
      <div class="d-inline-flex flex-column">
        {if (error.bind.nonEmpty) {
        <div class="text-danger">
          {error.bind}
        </div>
      } else {
        <span></span>
      }}{if (open.bind) {inputDisplay.bind} else <span></span>}<button type="button" class="btn btn-primary"
                                                                       disabled={disabled}
                                                                       onclick={onTip _}>Tip</button>
        <span>collected
          {s"${tipsCollected.bind} ${ui.thanks}hanks"}
        </span>
      </div>
      <div class="card-body">
        <h6 class="card-title">contribution</h6>
        <h6 class="card-subtitle mb-2 text-muted">by sarah</h6>
        <p class="card-text">
          body
          {withinOctopus.getBody.dom.bind}
        </p>
      </div>
    </div>
  }

  private def findTipModule = withinOctopus.getTopModule({ case m: ActionTip ⇒ m })

  private def onTip(e: Event) = {
    if (open.value) {
      if (tipping > 0) {
        findTipModule.foreach(_.add(tipping, UiContext.getUser))
        tipping = 1
        open.value_=(false)
      } else {
        error.value_=("must tip more than nothing")
      }
    } else open.value_=(!open.value)
  }

  @dom
  private def inputDisplay: Binding[Node] = <span>
    <input type="text" class="tip-input" value={tipping.toString}
           oninput={e: Event => onEnterTip(e)}></input>{ui.thanks}
  </span>

  private def onEnterTip(e: Event) = {
    println("entering tip")
    val v = e.srcElement.asInstanceOf[Input].value
    Try(v.toInt) match {
      case Success(t: Int) ⇒ println(s"s$t"); tipping = t; error.value_=("")
      case Failure(_) ⇒ println("f"); error.value_=("Not a round number")
    }
  }
}
