package gamestate.actions

import entities.Zombie
import gamestate.GameState
import upickle.default.{ReadWriter, macroRW}

final case class UpdateZombies(
                                id: Long,
                                time: Long,
                                zombiesInfo: List[ZombieUpdateInfo],
                                deletedZombies: List[Long]
                              ) extends GameAction {

  override def apply(gameState: GameState): GameState = gameState.updateZombies(
    time,
    zombiesInfo.map(info => info.id -> new Zombie(info.id, time, info.pos, info.dir, info.moving)).toMap,
    deletedZombies
  )

}

object UpdateZombies {

  implicit val readWriter: ReadWriter[UpdateZombies] = macroRW

}
