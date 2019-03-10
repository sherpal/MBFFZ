package gamedom

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement
import gamemanager.Game
import menus.Menus
import org.scalajs.dom
import org.scalajs.dom.html

final class GameInfo extends Component[html.Div] {

  private def formatTime(time: Long): String = "%d:%02d".format(time / 60, time % 60)

  val baseElement: ReactiveElement[html.Div] = div(
    div(
      "Time ",
      child <-- Game.$elapsedTime.map(_ / 1000).map(formatTime).map(label(_)),
      "s"
    ),
    div(
      ul(
        children <-- EventStream.fromFuture[List[ReactiveElement[dom.html.LI]]](Menus.liPlayerList)
      )
    )
  )

}
