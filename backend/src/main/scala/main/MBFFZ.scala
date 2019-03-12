package main

import routes.StaticFiles
import routes.game.{PostGame, PreGame}
import routes.menus.MenuRoutes
import routes.websockets.WebSocketRoute
import utils.Constants

/**
  * Main object that contains all the routes for the cask server.
  */
object MBFFZ extends cask.Main(
  MenuRoutes,
  PreGame,
  PostGame,
  StaticFiles,
  WebSocketRoute
) {

  override def host: String = Constants.hostName

  override def port: Int = Constants.port

  /**
    * Holds the necessary information to prevent browsers from caching.
    */
  val noCache: Seq[(String, String)] = Seq(
    "Cache-Control" -> "no-cache, no-store, must-revalidate",
    "Pragma" -> "no-cache",
    "Expires" -> "0"
  )

  override def main(args: Array[String]): Unit = {
    super.main(args)

    println(s"Server started on $host:$port") // print server info
  }

}
