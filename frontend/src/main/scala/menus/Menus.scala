package menus

import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.Protocol.HTTP
import fr.hmil.roshttp.body.PlainTextBody
import main.Main
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.html
import utils.Constants

import upickle.default._

import scala.concurrent.Future

object Menus {

  val root: String = s"http://${Constants.hostName}:${Constants.port}/"

  val boilerPlate: HttpRequest = HttpRequest()
    .withHost(Constants.hostName)
    .withPort(Constants.port)
    .withProtocol(HTTP)

  def playerList: Future[Map[String, String]] =
    (for (players <- boilerPlate.withPath("/pre-game/display-players").send()) yield players.body)
    .map(read[Map[String, String]](_))

  def launchButton: Future[String] =
    for (response <- boilerPlate.withPath("/pre-game/launch-button").send()) yield response.body

  private def launchGame(): Unit =
    boilerPlate
      .withPath("/pre-game/launch")
      .post(PlainTextBody(Main.password))

  Option(dom.document.getElementById("launch-game"))
    .map(_.asInstanceOf[html.Button])
    .foreach(_.onclick = (_: dom.Event) => launchGame())

  def moveToPostGame(): Unit =
    dom.window.location.href = root + "post-game"

}
