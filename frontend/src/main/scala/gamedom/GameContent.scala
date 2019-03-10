package gamedom

import com.raquo.laminar.api.L._
import org.scalajs.dom
import org.scalajs.dom.html
import utils.Constants

object GameContent {

  private val gameContentDiv: html.Div = dom.document.getElementById(Constants.gameContentId).asInstanceOf[html.Div]

  private val parentDiv = div(
    padding := "20px",
    display := "flex"
  )

  def apply(): GameCanvas = {
    val canvas = new GameCanvas
    val gameInfo = new GameInfo

    dom.document.getElementById(Constants.preGameContentId).asInstanceOf[dom.html.Div].style.display = "none"

    parentDiv.appendChild(
      canvas.baseElement
    )
    parentDiv.appendChild(
      gameInfo.baseElement
    )

    render(gameContentDiv, parentDiv)

    canvas
  }

}
