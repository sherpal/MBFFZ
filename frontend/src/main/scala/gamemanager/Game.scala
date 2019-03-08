package gamemanager

import com.raquo.airstream.core.Observer
import com.raquo.airstream.ownership.Owner
import entities.Player
import gamedrawer.GameDrawer
import gamestate.{ActionCollector, GameState}
import gamestate.actions.{GameAction, UpdatePlayerPos}
import org.scalajs.dom
import websockets.Communicator
import entities.Player.{Down, Left, Right, Up}

final class Game private (val myId: Long) {

  println("Game is about to start.")
  println(s"My id is $myId")

  GameDrawer.showCanvas()

  private val actionCollector: ActionCollector = new ActionCollector(
    GameState.emptyGameState()
  )

  def receiveActions(actions: List[GameAction]): Unit = {
    actionCollector.addActions(actions)
  }

  def receiveAction(action: GameAction): Unit = {
    actionCollector.addAction(action)
  }

  private def updatePlayersPredictions(players: List[Player], currentTime: Long): List[UpdatePlayerPos] = {
    players.filterNot(_.id == myId).map(player => (player, player.currentPosition(currentTime - player.time)))
      .map({
        case (player, newPosition) =>
          UpdatePlayerPos(0, currentTime, player.id, newPosition, player.direction, player.moving)
      })

  }

  @inline def getTime: Long = Communicator.communicator.getTime

  private def move(time: Long, gs: GameState): Option[UpdatePlayerPos] = gs.players.get(myId).map {
    player =>
      val newPlayer = Player.updatePlayer(
        time, player,
        List(Up, Down, Right, Left).filter(GameDrawer.isPressed),
        gs.quadtree, GameState.worldBox
      )

      UpdatePlayerPos(0, time, newPlayer.id, newPlayer.pos, newPlayer.direction, newPlayer.moving)
  }

  private def run(lastTime: Long): Unit = {
    val time = getTime

    // val delta: Long = time - lastTime

    val currentGameState = actionCollector.currentGameState

    val updatePlayerPos = move(time, currentGameState)

    if (updatePlayerPos.isDefined) {
      Communicator.communicator.sendMessage(updatePlayerPos.get)
    }

    val predictedGameState = currentGameState(
      updatePlayerPos.map(List(_)).getOrElse(Nil) ++
        updatePlayersPredictions(currentGameState.players.values.toList, getTime)
    )

    GameDrawer.drawGameState(predictedGameState)

    dom.window.requestAnimationFrame((_: Double) => run(time))
  }

  dom.window.requestAnimationFrame((_: Double) => run(getTime))

}

object Game extends Owner {

  private var _game: Option[Game] = None

  @inline def game: Game = _game.get

  def apply(id: String): Game = {
    _game = Some(new Game(id.toLong))
    game
  }

  Communicator.communicator.$wsStringMessage
    .filter(_.startsWith("id:"))
    .map(_.substring(3))
    .addObserver(Observer(id => Game(id)))(this)

}