package ai

import ai.game.GameTrainer
import outsidemessages.NewGame
import routes.websockets.Client

import scala.collection.mutable

object AIManager {

  /**
    * Map from game names to corresponding server
    */
  private val servers: mutable.Map[String, AIWebSocketServer] = mutable.Map()

  def gameServer(gameName: String): AIWebSocketServer = servers(gameName)

  def clientConnect(newGame: NewGame, client: Client): Unit = {
    if (!servers.isDefinedAt(newGame.gameName))
      servers += newGame.gameName -> new AIWebSocketServer(newGame.gameName)

    gameServer(newGame.gameName).clientConnected(client, newGame.aiName)
  }

  private val gameTrainers: mutable.Map[String, GameTrainer] = mutable.Map()

  def gameHasStarted(gameName: String): Boolean = gameTrainers.isDefinedAt(gameName)

  def launchGame(gameName: String, timeStamp: Long, players: List[String]): Unit =
    gameTrainers += gameName -> new GameTrainer(gameName, timeStamp, players)

  @inline def gameTrainer(gameName: String): GameTrainer = gameTrainers(gameName)

  def gameEnds(gameName: String): Unit = {
    gameTrainers -= gameName
  }

}
