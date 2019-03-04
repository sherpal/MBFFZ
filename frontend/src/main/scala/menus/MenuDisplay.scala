package menus

import org.scalajs.dom
import org.scalajs.dom.html
import utils.Constants
import monix.execution.Scheduler.Implicits.global
import com.raquo.laminar.api.L._

import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

object MenuDisplay {

  implicit private class VarWithAssignOp[A](v: Var[A]) {
    def :=(a: A): Unit = v.set(a)
  }

  private def delay(body: => Unit): SetTimeoutHandle = setTimeout(0)(body)

  private val childrenVar: Var[List[Element]] = Var(List())

  def changePlayerList(): Unit = delay {
    for (content <- Menus.playerList) {
      childrenVar := content.toList.sorted.map {
          case (playerName, playerColour) =>
            li(playerName, color := playerColour)
        }
    }
  }

  private val playerList: Element = ul(children <-- childrenVar.signal)

  render(dom.document.getElementById(Constants.preGamePlayerListULId), playerList)

  private lazy val launchButtonContainer: html.Div = dom.document.getElementById("launch-btn-container")
    .asInstanceOf[html.Div]

  def setLaunchButton(): Unit = delay {
    for (content <- Menus.launchButton) {
      launchButtonContainer.innerHTML = content
    }
  }

}
