package gamestate.actions

import gamestate.GameState

final case class PlayerDies(id: Long, time: Long, playerId: Long) extends GameAction {

  def apply(gameState: GameState): GameState = gameState.removePlayer(time, playerId)

}

object PlayerDies {
  import upickle.default.{ReadWriter, macroRW}
  implicit val readWriter: ReadWriter[PlayerDies] = macroRW
}
