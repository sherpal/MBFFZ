package ai.game

import ai.{AIManager, AIWebSocketServer}
import entities.{PopStationContainer, Zombie}
import gamestate.actions.{GameAction, NewPlayerInfo, UpdatePlayerPos}
import gamestate.{ActionCollector, GameState}
import outsidemessages.{Direction, YouHaveDied}

import upickle.default._

import scala.collection.mutable

/**
  * Allows an AI to train by playing the game super-fast.
  *
  * @param gameName name of the game, useful to know which AIWebSocketServer is in charge
  * @param timeStamp time that separates two consecutive game states the AI will have access to,
  *                  in milliseconds. (500ms seems like a good number)
  * @param aiNames the names of all the artificial intelligence that will play the game. Unlike an
  *                actual game, there can be only one player. The game in this case terminates when
  *                the last AI player dies.
  */
final class GameTrainer(gameName: String, timeStamp: Long, aiNames: List[String]) {

  println(s"Starting game $gameName.")
  println(s"AIs participating are ${aiNames.mkString(", ")}")
  println(s"Time stamp is $timeStamp.")

  val server: AIWebSocketServer = AIManager.gameServer(gameName)

  val nbrOfPlayers: Int = aiNames.length

  private val actionCollector: ActionCollector = new ActionCollector(GameState.emptyGameState())
  private val initialGameState = GameState.startingGameState(playerColours = aiNames).copy(time = 0)

  actionCollector.addAction(initialGameState.copy(computeGraph = true))
  private val quadTree = actionCollector.currentGameState.quadtree

  private val playerIds: Map[String, Long] =
    (for (NewPlayerInfo(id, _, name) <- initialGameState.playersInfo) yield name -> id).toMap

  private val namesFromIds: Map[Long, String] = playerIds.map(_.swap)

  for ((name, id) <- playerIds) {
    server.sendMessageWithType("playerId", id.toString, name)
  }

  /**
    * Remembers the directions received by all the AIs.
    * Once we received the directions from everyone, we proceed to the next game state.
    */
  private val bufferedMessages: mutable.Map[Long, Direction] = mutable.Map()

  def receiveDirections(direction: Direction): Unit = {
    bufferedMessages += playerIds(direction.playerName) -> direction

    if (bufferedMessages.size == nbrOfPlayers) {
      nextGameState()
    }
  }

  private def nextGameState(): Unit = {
    val newTime = actionCollector.currentGameState.time + timeStamp

    /** AIs move respectively to their choices of directions */
    actionCollector.addActions(
      for ((id, direction) <- bufferedMessages) yield {
        val player = actionCollector.currentGameState.players(id)
        val (dir, moving) = direction.directionAndMoving
        UpdatePlayerPos(
          GameAction.id(), newTime, id,
          quadTree.boundingBox.clampToSelf(
            player.lastValidPos(newTime - player.time, actionCollector.currentGameState.quadtree, dir)
          ),
          dir, moving
        )
      }
    )

    val gs = actionCollector.currentGameState

    val (newPopStations, poppedStations) = gs.popStationContainer.updateZombiePopStations(
      newTime, ((newTime - gs.startTime) / 30000 + nbrOfPlayers).toInt,
      math.max(2500: Long, PopStationContainer.popTime - ((newTime - gs.startTime) / 60000) * 2000),
      gs.quadtree
    )

    actionCollector.addActions(List(newPopStations, poppedStations).flatten)
    actionCollector.addAction(Zombie.updateZombies(
      newTime, gs.zombies, gs.players.values.toSet, gs.graph, gs.quadtree, gs.inflatedEdges
    ))

    val deadPlayers = Zombie.findDeadPlayers(gs.players.values, gs.zombies.values, newTime)

    actionCollector.addActions(deadPlayers)

    for ((name, message) <- deadPlayers.map(playerDies => (namesFromIds(playerDies.playerId), YouHaveDied(newTime)))) {
      server.sendMessageWithType("YouHaveDied", write(message), name)
    }

    server.broadcastGameState(actionCollector.currentGameState)

    if (actionCollector.currentGameState.players.isEmpty) {
      AIManager.gameEnds(gameName)
    }

  }


}
