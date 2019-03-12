package routes.game

import cask.model.Response
import gamemanager.PreGameManager
import gamemanager.exceptions.GameException
import main.MBFFZ
import routes.RedirectWithoutCaching
import upickle.default._
import scalatags.Text.all._
import utils.Constants

object PreGame extends cask.Routes {

  val cookieName: String = "enterGamePassword"

  @cask.get("/pre-game")
  def preGame(request: cask.Request): Response =
  if (!request.cookies.get(cookieName).map(_.value).exists(PreGameManager.passwordExists)) {
    RedirectWithoutCaching("/")
  } else {

    val enterGamePassword = request.cookies(cookieName)
    val playerName = PreGameManager.playerName(enterGamePassword.value)

    val htmlBody = "<!doctype html>" + html(
      head(
        meta(
          charset := "utf-8"
        )
      ),
      body(
        div(
          id := Constants.preGameContentId,
          h1("Game joined"),
          h2(s"You joined the game as $playerName"),
          div(
            backgroundColor := "#ccc",
            border := "1px solid black",
            padding := "20px",
            borderRadius := "5px",
            div("Players:"),
            div(
              id := Constants.preGamePlayerListULId,
            ),
            div(
              id := Constants.launchButtonContainerId
            )
          )
        ),
        div(
          id := Constants.gameContentId,
          display := "flex",
          ""
        ),
        script(
          `type` := "text/javascript",
          src := "app/frontend-scala.js"
        )
      )
    )

    cask.Response(htmlBody, headers = MBFFZ.noCache)
  }

  /** Returns the map from player names to player colours. */
  @cask.get("/pre-game/players")
  def players(): String = write(PreGameManager.players)

  /** Gets the player name associated to the password. */
  @cask.get("/pre-game/me/:password")
  def me(password: String): String = write(PreGameManager.playerName(password))

  /** Why is this there? I think it's from the past. */
  @cask.get("/pre-game/display-players")
  def displayPlayers(): String = write(PreGameManager.players)

  /** Returns content needed to have the launch button. */
  @cask.get("/pre-game/launch-button")
  def launchButton(): String = button(id := "launch-game", "Launch Game").render

  /** Launches the game, but only if the head requests it. */
  @cask.post("/pre-game/launch")
  def launchGame(request: cask.Request): cask.Response = {
    val password = new String(request.readAllBytes())

    try {
      PreGameManager.startGame(password)
      cask.Response("OK", 200)
    } catch {
      case e: GameException =>
        cask.Response(e.message, 400)
      case e: Throwable =>
        throw e
    }
  }

  initialize()
}
