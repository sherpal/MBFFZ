package outsidemessages

final case class NewGame(gameName: String, aiName: String)

object NewGame {
  import upickle.default.{ReadWriter, macroRW}
  implicit val readWriter: ReadWriter[NewGame] = macroRW
}
