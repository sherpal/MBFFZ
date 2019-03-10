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

  private val boilerPlate: HttpRequest = HttpRequest()
    .withHost(Constants.hostName)
    .withPort(Constants.port)
    .withProtocol(HTTP)

  def playerList: Future[Map[String, String]] =
    (for (players <- boilerPlate.withPath("/pre-game/display-players").send()) yield players.body)
    .map(read[Map[String, String]](_))

  def liPlayerList: Future[List[ReactiveElement[html.LI]]] = playerList
    .map(_.mapValues(Player.playerColours))
    .map(_.toList.sorted)
    .map(_.map({ case (playerName, playerColour) => li(playerName, color := playerColour) }))

  def launchGame(): Unit =
    boilerPlate
      .withPath("/pre-game/launch")
      .post(PlainTextBody(Main.password))

  def moveToPostGame(): Unit =
    dom.window.location.href = root + "post-game"

}
