package gamemanager

import entities.{PopStationContainer, Zombie}
import gamestate.actions.{GameAction, GameEnd, PlayerDies, UpdatePlayerPos}
import gamestate.{ActionCollector, GameState}
import messages.Message.ActionList

import scala.collection.mutable

/**
  * Starts the game.
  * @param playersInfo map from player names to their colours
  * @param passwords   map from player names to their password (/!\ it is not the same order as in
  *                    the [[PreGameManager]])
  */
final class GameManager(playersInfo: Map[String, String], passwords: Map[String, String]) {

  private val playersFromColours: Map[String, String] = playersInfo.map(_.swap)

  val nbrOfPlayers: Int = playersInfo.size

  val server: GameServer = Manager.server

  private val actionCollector: ActionCollector = new ActionCollector(GameState.emptyGameState())

  private def playerName(id: Long): String = playersFromColours(actionCollector.currentGameState.players(id).colour)

  Manager.setState(Manager.PlayingState)

  private val initialGameState = GameState.startingGameState(playerColours = playersInfo.values.toList)

  private val playerIds = initialGameState.playersInfo.map {
    case gamestate.actions.NewPlayerInfo(id, _, colour) =>
      passwords(playersInfo.find(_._2 == colour).get._1) -> id
  }

  actionCollector.addAction(initialGameState.copy(computeGraph = true))

  for ((password, id) <- playerIds) {
    server.sendPlayerId(id, password)
  }

  println("Game Should Start now.")

  private val queuedActions: mutable.Queue[GameAction] = mutable.Queue()

  def receivedAction(action: GameAction): Unit = {
    action match {
      case updatePlayerPos: UpdatePlayerPos
        if actionCollector.currentGameState.players.isDefinedAt(updatePlayerPos.playerId) =>
        enqueue(updatePlayerPos)
      case updatePlayerPos: UpdatePlayerPos =>
        println(s"Player ${updatePlayerPos.playerId} is dead.")
      case _ =>
        // should not happen
    }
  }

  def enqueue(action: GameAction): Unit = this.synchronized {
    actionCollector.addAction(action)
    queuedActions.enqueue(action)
  }

  def enqueueActions(actions: Seq[GameAction]): Unit = this.synchronized {
    for (action <- actions) queuedActions.enqueue(action)

    actionCollector.addActions(actions)
  }

  def flushActions(): ActionList = this.synchronized {
    val actions = ActionList(queuedActions.toList)
    queuedActions.clear()

    actions
  }

  def sendActions(): Unit = {
    val actions = flushActions()

    if (actions.gameActions.nonEmpty) {
      server.broadcastMessage(actions)
    }
  }

  private val orderOfDeaths: mutable.Set[(String, Long)] = mutable.Set()

  private var gamePlaying: Boolean = true

  private lazy val flushThread: Thread = new Thread {
    override def run(): Unit = {
      sendActions()

      Thread.sleep(30)

      if (gamePlaying) {
        run()
      }
    }
  }

  private lazy val gameThread: Thread =
    new Thread {
      server.broadcastMessage(initialGameState)

      override def run(): Unit = {
        val gs = actionCollector.currentGameState

        val time = new java.util.Date().getTime

        val (newPopStations, poppedStations) = gs.popStationContainer.updateZombiePopStations(
          time, ((time - gs.startTime) / 30000 + nbrOfPlayers).toInt,
          math.max(2500: Long, PopStationContainer.popTime - ((time - gs.startTime) / 60000) * 2000),
          gs.quadtree
        )

        enqueueActions(List(newPopStations, poppedStations).flatten)

        val zombieUpdate = Zombie.updateZombies(
          time, gs.zombies, gs.players.values.toSet, gs.graph, gs.quadtree, gs.inflatedEdges
        )
        enqueue(zombieUpdate)

        val deadPlayers = Zombie.findDeadPlayers(gs.players.values, gs.zombies.values, time)

        for (PlayerDies(_, time, playerId) <- deadPlayers) {
          orderOfDeaths += playerName(playerId) -> (time - gs.startTime)
        }
        enqueueActions(deadPlayers)

        Thread.sleep(math.max(1000 / 30 - (new java.util.Date().getTime - time), 30))

        if (actionCollector.currentGameState.players.size <= 1) {
          val gs = actionCollector.currentGameState

          sendActions()

          val gameEnd = GameEnd(
            GameAction.id(),
            new java.util.Date().getTime,
            if (gs.players.isEmpty) "The last players died at the same time..."
            else s"The winner is ${playerName(gs.players.head._1)}"
          )

          orderOfDeaths ++= gs.players.keys.toList
            .map(id => playerName(id) -> (new java.util.Date().getTime - gs.startTime))

          gameEnds(gameEnd)
          server.broadcastMessage(gameEnd)
        } else {
          run()
        }
      }
    }


  def startThread(): Unit = {
    gameThread.start()
    flushThread.start()
  }

  private def gameEnds(gameEnd: GameEnd): Unit = {
    Manager.endGame(gameEnd, orderOfDeaths.toList.sortBy(_._2).reverse)
    gamePlaying = false
  }

}
