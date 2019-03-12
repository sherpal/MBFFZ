package gamemanager

import com.raquo.airstream.core.Observer
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.ownership.Owner
import entities.Player
import gamedrawer.GameDrawer
import gamestate.{ActionCollector, GameState}
import gamestate.actions.{GameAction, UpdatePlayerPos}
import org.scalajs.dom
import websockets.Communicator

import scala.scalajs.js.timers.setInterval

/**
  * Contains the game loop.
  *
  * In this game the game loop is quite simple:
  * - put all players at their current position
  * - move this player according to the pressed keys
  * - draw the game
  */
final class Game private (val myId: Long) extends Owner {

  /** Being fancy for nothing. */
  private implicit class ObserverWithArrow[T](callback: T => Unit) {
    def <--(stream: EventStream[T]): Unit =
      stream.addObserver(Observer(callback))(Game.this)
  }

  if (scala.scalajs.LinkingInfo.developmentMode) {
    println("Game is about to start.")
    println(s"My id is $myId")
  }

  GameDrawer

  /** Initialize the ActionCollector */
  private val actionCollector: ActionCollector = new ActionCollector(GameState.emptyGameState())

  /** Registering to the GameAction flow from the [[Communicator]] */
  ((action: GameAction) => actionCollector.addAction(action)) <-- Communicator.$gameAction

  /** Maps every other player to their current positions. */
  private def updatePlayersPredictions(players: List[Player], currentTime: Long): List[UpdatePlayerPos] = {
    players.filterNot(_.id == myId).map(player => (player, player.currentPosition(currentTime - player.time)))
      .map({
        case (player, newPosition) =>
          UpdatePlayerPos(0, currentTime, player.id, newPosition, player.direction, player.moving)
      })

  }

  @inline def getTime: Long = Communicator.getTime

  /** Moves this player according to the pressed keys. */
  private def move(time: Long, gs: GameState): Option[UpdatePlayerPos] = gs.players.get(myId).map {
    player =>
      val newPlayer = Player.updatePlayer(
        time, player,
        Player.Direction.directions.filter(GameDrawer.isPressed),  // easy way to handle the 9 possibilities
        gs.quadtree, GameState.worldBox
      )

      UpdatePlayerPos(0, time, newPlayer.id, newPlayer.pos, newPlayer.direction, newPlayer.moving)
  }

  /**
    * Game loop.
    *
    * This uses the `refreshAnimationFrame`
    *
    * @param lastTime last time the loop ran
    */
  private def run(lastTime: Long): Unit = {
    val time = getTime

    // val delta: Long = time - lastTime

    val currentGameState = actionCollector.currentGameState

    val updatePlayerPos = move(time, currentGameState)

    if (updatePlayerPos.isDefined) {
      Communicator.sendMessage(updatePlayerPos.get)
    }

    val predictedGameState = currentGameState(
      updatePlayerPos.map(List(_)).getOrElse(Nil) ++
        updatePlayersPredictions(currentGameState.players.values.toList, getTime)
    )

    GameDrawer.drawGameState(predictedGameState)

    dom.window.requestAnimationFrame((_: Double) => run(time))
  }

  dom.window.requestAnimationFrame((_: Double) => run(getTime))


  /**
    * Each second, we push through the elapsedTime stream the number of milliseconds elapsed since the beginning of
    * the game.
    */
  setInterval(1000) {
    Game.elapsedTimeBus.writer.onNext(Communicator.getTime - actionCollector.currentGameState.startTime)
  }


}

object Game extends Owner {

  private val elapsedTimeBus: EventBus[Long] = new EventBus[Long]()
  def $elapsedTime: EventStream[Long] = elapsedTimeBus.events

  private var _game: Option[Game] = None

  @inline def game: Game = _game.get

  private def apply(id: String): Game = {
    _game = Some(new Game(id.toLong))
    game
  }

  Communicator.$wsStringMessage
    .filter(_.startsWith("id:"))
    .map(_.substring(3))
    .addObserver(Observer(id => Game(id)))(this)

}
