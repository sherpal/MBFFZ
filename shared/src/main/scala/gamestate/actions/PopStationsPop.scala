package gamestate.actions

import gamestate.GameState
import upickle.default.{ReadWriter, macroRW}

final case class PopStationsPop(
                                 id: Long, time: Long,
                                 popStationIds: List[Long], zombieIds: List[Long]
                               ) extends GameAction {

  def apply(gameState: GameState): GameState = gameState.stationsPop(
    time, popStationIds, zombieIds
  )

}

object PopStationsPop {

  implicit val readWriter: ReadWriter[PopStationsPop] = macroRW

}