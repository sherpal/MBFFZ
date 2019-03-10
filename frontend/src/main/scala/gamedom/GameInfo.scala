package gamedom

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement
import gamemanager.Game
import menus.Menus
import org.scalajs.dom
import org.scalajs.dom.html

final class GameInfo extends Component[html.Div] {

  val baseElement: ReactiveElement[html.Div] = div(
    div(
      "Time ",
      child <-- Game.$elapsedTime.map(_ / 1000).map(t => label(t.toString)),
      "s"
    ),
    div(
      ul(
        children <-- EventStream.fromFuture[List[ReactiveElement[dom.html.LI]]](Menus.liPlayerList)
      )
    )
  )

}
