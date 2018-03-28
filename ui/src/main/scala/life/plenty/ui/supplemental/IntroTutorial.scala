package life.plenty.ui.supplemental

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.User
import life.plenty.ui
import life.plenty.ui.display.Modal
import org.scalajs.dom.Node

object IntroTutorial {
  def apply(user: User): Unit = {
    Modal.giveContentAndOpen(this, content, "tutorial-modal-box")
  }

  @dom
  private def content: Binding[Node] = {
    <div class="tutorial-container">
      <div>
        <img class="header-img" src="http://about.plenty.life/etc/plenty_logo_3b_header_low_res.png"/>
      </div>
      <p>
        Use {ui.plenty.bind} to organize clubs, events or any on-going meetup.
      </p>
      <p>
        Listen to this audio tutorial to get the gist of how {ui.plenty.bind} works<br/>
          <audio controls={true}>
            <!--<source src="horse.ogg" type="audio/ogg">-->
            <source src="http://about.plenty.life/etc/tutorial.mp3" type="audio/mpeg"/>
              Your browser does not support the audio element.
          </audio>
      </p>
      <p>
        <a class="vision-link" href="http://about.plenty.life" target="_blank"> Read about our vision</a>
      </p>
      <p>
        Please keep in mind that currently {ui.plenty.bind} is a <b>prototype</b> and does not provide a polished
        experience.
      </p>
      <p>
        <a href="http://about.plenty.life/#message-us" target="_blank">Message us</a> with any questions or comments
      </p>
    </div>
  }
}
