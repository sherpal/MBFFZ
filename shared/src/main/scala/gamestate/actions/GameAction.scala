package gamestate.actions

import gamestate.GameState
import messages.Message

trait GameAction extends Message {

  val id: Long
  val time: Long

  def apply(gameState: GameState): GameState

}

object GameAction {

  private var lastId: Long = 0

  def id(): Long = {
    lastId += 1
    lastId
  }

}