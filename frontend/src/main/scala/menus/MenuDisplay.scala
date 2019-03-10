package menus

import com.raquo.airstream.features.FlattenStrategy
import org.scalajs.dom
import utils.Constants
import com.raquo.laminar.api.L._
import websockets.Communicator

object MenuDisplay extends Owner {

  implicit private val strategy: FlattenStrategy.ConcurrentFutureStrategy.type = ConcurrentFutureStrategy

  private val playerListStream: EventStream[List[Element]] = Communicator.communicator.$wsStringMessage
    .filter(_ == Constants.playerListUpdate)
    .map((_: String) => Menus.liPlayerList)
    .flatten
  private val playerList: Element = ul(children <-- playerListStream)
  render(dom.document.getElementById(Constants.preGamePlayerListULId), playerList)


  private val launchGameBus = new EventBus[dom.MouseEvent]()
  private val $launchGame = launchGameBus.events

  $launchGame
    .filter(_.button == 0)
    .addObserver(Observer((_: dom.MouseEvent) => Menus.launchGame()))(this)

  private val launchButtonStream = Communicator.communicator.$wsStringMessage
    .filter(_ == Constants.youAreTheHead)
    .map((_: String) => button("Launch Game", inContext(_ => onClick.map(ev => ev) --> launchGameBus)))
  private val launchButtonWrapper: Element = div(child <-- launchButtonStream)
  render(dom.document.getElementById(Constants.launchButtonContainerId), launchButtonWrapper)

}
