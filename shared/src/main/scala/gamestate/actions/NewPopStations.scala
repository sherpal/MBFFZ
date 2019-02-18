package gamestate.actions

import entities.ZombiePopStation
import gamestate.GameState
import physics.Complex

final case class NewPopStations(
                                id: Long, time: Long,
                                popInfo: List[(Long, Complex)]
                              ) extends GameAction {

  def apply(gameState: GameState): GameState = gameState.addPopStations(
    time, popInfo.map(info => info._1 -> new ZombiePopStation(info._1, time, info._2)).toMap
  )

}

object NewPopStations {

  import upickle.default.{ReadWriter, macroRW}

  implicit val readWriter: ReadWriter[NewPopStations] = macroRW

}
