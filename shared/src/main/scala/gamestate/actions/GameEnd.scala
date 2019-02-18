package gamestate.actions

import gamestate.GameState

final case class GameEnd(id: Long, time: Long, endingMessage: String) extends GameAction {

  def apply(gameState: GameState): GameState = gameState

}

object GameEnd {
  import upickle.default.{ReadWriter, macroRW}
  implicit val readWriter: ReadWriter[GameEnd] = macroRW
}
