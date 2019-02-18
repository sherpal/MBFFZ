package main

import routes.StaticFiles
import routes.game.{PostGame, PreGame}
import routes.menus.MenuRoutes
import routes.websockets.WebSocketRoute
import utils.Constants

object MBFFZ extends cask.Main(
  MenuRoutes,
  PreGame,
  PostGame,
  StaticFiles,
  WebSocketRoute
) {

  override def host: String = Constants.hostName

  override def port: Int = Constants.port

  val noCache: Seq[(String, String)] = Seq(
    "Cache-Control" -> "no-cache, no-store, must-revalidate",
    "Pragma" -> "no-cache",
    "Expires" -> "0"
  )

}
