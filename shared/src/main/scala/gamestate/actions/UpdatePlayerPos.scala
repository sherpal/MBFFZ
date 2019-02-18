package gamestate.actions

import entities.Player
import gamestate.GameState
import physics.Complex
import upickle.default.{ReadWriter, macroRW}

final case class UpdatePlayerPos(
                                  id: Long, time: Long,
                                  playerId: Long,
                                  pos: Complex,
                                  direction: Double, moving: Boolean
                                ) extends GameAction {

  def apply(gameState: GameState): GameState =
    if (gameState.players.isDefinedAt(playerId))
    gameState.updatePlayer(
      time, new Player(playerId, time, pos, direction, moving, gameState.players(playerId).colour)
    )
    else gameState

}

object UpdatePlayerPos {

  implicit val readWriter: ReadWriter[UpdatePlayerPos] = macroRW

}