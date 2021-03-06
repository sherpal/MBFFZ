package routes.menus

import cask.model.Cookie
import entities.Player
import gamemanager.PreGameManager
import gamemanager.exceptions.GameException
import main.MBFFZ
import routes.game.PreGame
import scalatags.Text.all._
import upickle.default._

object MenuRoutes extends cask.Routes {

  /** Returns the list of available colours, wrapped in option tags. */
  private def remainingColours: List[Tag] =
    (Player.playerColours.keys.toSet -- PreGameManager.usedColours)
      .toList.sorted.map(colour => option(value := colour, colour))

  /** HTML of the welcome page. */
  @cask.get("/")
  def hello(): cask.Response = {

    cask.Response(
      "<!doctype html>" + html(
        head(
          meta(
            charset := "utf-8"
          )
        ),
        body(
          h1("Welcome to MBFFZ"),
          h2("The amazing multi-player version of BFFZ"),
          form(
            id := "join-form",
            div(
              padding := "10px",
              border := "1px solid black",
              label(marginRight := "5px", "Player name"),
              input(
                `type` := "text",
                id := "player-name"
              )
            ),
            div(
              marginTop := "5px",
              padding := "10px",
              border := "1px solid black",
              label("Chose your in-game colour"),
              select(
                id := "colour-select",
                remainingColours
              )
            ),
            div(
              marginTop := "5px",
              button(
                id := "submit",
                "Join Game"
              )
            )
          ),
          script(
            `type` := "text/javascript",
            src := "app/index.js"
          )
        )
      ),
      cookies = Seq(cask.Cookie(PreGame.cookieName, "", expires = java.time.Instant.EPOCH))
    )
  }

  /**
    * Gives the remaining colours to the client.
    * This is used when a player was trying to connect with an already used colour.
    */
  @cask.get("/menus/colours")
  def returnColours(): String = remainingColours.map(_.render).mkString

  /**
    * Joins the game with the given name and colour, if still available.
    *
    * This should be a post as it has a side effect...
    */
  @cask.get("/player-join-form/:playerName/:playerColour")
  def playerJoin(playerName: String, playerColour: String): cask.Response = {
    try {
      val password = PreGameManager.addPlayer(playerName, playerColour)

      cask.Response(
        data = "You've joined",
        statusCode = 200,
        headers = MBFFZ.noCache,
        cookies = List(Cookie(PreGame.cookieName, password, path = "/"))
      )
    } catch {
      case e: GameException =>
        cask.Response(
          data = e.getMessage,
          statusCode = 400
        )
      case e: Throwable =>
        throw e
    }
  }

  /** Not sure whether I'm still using this... */
  @cask.get("/player-colours")
  def colours(): String = write(Player.playerColours, indent = 2)

  initialize()

}
