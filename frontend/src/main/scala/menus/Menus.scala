package menus

import main.Main

import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.api.L._
import entities.Player
import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.Protocol.HTTP
import fr.hmil.roshttp.body.PlainTextBody
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.html
import utils.Constants
import upickle.default._

import scala.concurrent.Future

object Menus {

  val root: String = s"http://${Constants.hostName}:${Constants.port}/"

  /**
    * Some boilerplate for http requests.
    */
  private val boilerPlate: HttpRequest = HttpRequest()
    .withHost(Constants.hostName)
    .withPort(Constants.port)
    .withProtocol(HTTP)

  /** Gets the list of connected players, with their colours */
  def playerList: Future[Map[String, String]] =
    (for (players <- boilerPlate.withPath("/pre-game/display-players").send()) yield players.body)
    .map(read[Map[String, String]](_))

  /** Gets the list of connected players, as list of li (reactive) elements. */
  def liPlayerList: Future[List[ReactiveElement[html.LI]]] = playerList
    .map(_.mapValues(Player.playerColours))
    .map(_.toList.sorted)
    .map(_.map({ case (playerName, playerColour) => li(playerName, color := playerColour) }))

  /** Sends a post request to launch the game. Only the head player is supposed to be allowed to do that. */
  def launchGame(): Unit =
    boilerPlate
      .withPath("/pre-game/launch")
      .post(PlainTextBody(Main.password))

  /** When the game ends, we move to the last page. */
  def moveToPostGame(): Unit =
    dom.window.location.href = root + "post-game"

}
