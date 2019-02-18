package gamestate.actions

import messages.Message
import physics.Complex
import upickle.default.{ReadWriter, macroRW}

final case class ZombieUpdateInfo(id: Long, pos: Complex, dir: Double, moving: Boolean) extends Message

object ZombieUpdateInfo {

  implicit val readWrite: ReadWriter[ZombieUpdateInfo] = macroRW

}