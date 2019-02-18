package menus

import org.scalajs.dom
import org.scalajs.dom.html
import utils.Constants
import monix.execution.Scheduler.Implicits.global

import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

object MenuDisplay {

  private def delay(body: => Unit): SetTimeoutHandle = setTimeout(0)(body)

  private val playerList: html.UList = dom.document.getElementById(Constants.preGamePlayerListULId)
    .asInstanceOf[html.UList]

  def changePlayerList(): Unit = delay {
    for (content <- Menus.playerList) {
      playerList.innerHTML = content
    }
  }

  private lazy val launchButtonContainer: html.Div = dom.document.getElementById("launch-btn-container")
    .asInstanceOf[html.Div]

  def setLaunchButton(): Unit = delay {
    for (content <- Menus.launchButton) {
      launchButtonContainer.innerHTML = content
    }
  }

}
