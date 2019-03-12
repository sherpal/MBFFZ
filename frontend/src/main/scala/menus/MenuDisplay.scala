package menus

import com.raquo.airstream.features.FlattenStrategy
import org.scalajs.dom
import utils.Constants
import com.raquo.laminar.api.L._
import websockets.Communicator

object MenuDisplay extends Owner {

  /** Weird airstream business. */
  implicit private val strategy: FlattenStrategy.ConcurrentFutureStrategy.type = ConcurrentFutureStrategy

  /** We ask for the player list when the Communicator receives a `playerListUpdate` string message. */
  private val playerListStream: EventStream[List[Element]] = Communicator.$wsStringMessage
    .filter(_ == Constants.playerListUpdate)
    .map((_: String) => Menus.liPlayerList)
    .flatten
  /** Simply the list of currently connected player. Laminar magic here. */
  private val playerList: Element = ul(children <-- playerListStream)
  render(dom.document.getElementById(Constants.preGamePlayerListULId), playerList)


  private val launchGameBus = new EventBus[dom.MouseEvent]()
  private val $launchGame = launchGameBus.events

  $launchGame
    .filter(_.button == 0)
    .addObserver(Observer((_: dom.MouseEvent) => Menus.launchGame()))(this)

  /** Creates the launch game button when we receive the `youAreTheHead` message. */
  private val launchButtonStream = Communicator.$wsStringMessage
    .filter(_ == Constants.youAreTheHead)
    .map((_: String) => button("Launch Game", inContext(_ => onClick.map(ev => ev) --> launchGameBus)))
  /** Contains the launch game button, when we are the head. Laminar magic here. */
  private val launchButtonWrapper: Element = div(child <-- launchButtonStream)
  render(dom.document.getElementById(Constants.launchButtonContainerId), launchButtonWrapper)

}
