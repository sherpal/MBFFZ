package gamestate.actions

import messages.Message
import physics.Complex

final case class NewPlayerInfo(id: Long, pos: Complex, colour: String) extends Message

object NewPlayerInfo {
  import upickle.default.{ReadWriter, macroRW}
  implicit val readWriter: ReadWriter[NewPlayerInfo] = macroRW
}
