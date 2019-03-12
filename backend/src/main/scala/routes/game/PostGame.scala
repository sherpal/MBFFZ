package routes.game

import gamemanager.Manager
import routes.RedirectWithoutCaching
import scalatags.Text.all._
import utils.Constants

object PostGame extends cask.Routes {

  /** Displays the post game content if it exists. */
  @cask.get("/post-game")
  def scoreboard(): cask.Response = {
    Manager.postGameManager match {
      case Some(postGameManager) =>
        "<!doctype html>" + html(
          head(
            meta(
              charset := "utf-8"
            )
          ),
          body(
            h1("Game Over"),
            h2("Scoreboard"),
            div(
              postGameManager.gameEnd.endingMessage
            ),
            ol(
              postGameManager.orderOfDeaths.map {
                case (playerName, time) =>
                  li(
                    s"$playerName: ${time / 1000}s"
                  )
              }
            ),
            div(
              a(
                href := s"http://${Constants.hostName}:${Constants.port}/",
                backgroundColor := "#ccc",
                padding := "3px",
                border := "1px solid black",
                color := "black",
                textDecoration := "none",
                "Back to menu"
              )
            )
          )
        )
      case None =>
        RedirectWithoutCaching("/")
    }
  }

  initialize()

}
