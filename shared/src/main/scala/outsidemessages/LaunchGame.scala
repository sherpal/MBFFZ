package outsidemessages

final case class LaunchGame(gameName: String, timeStamp: Long)

object LaunchGame {
  import upickle.default.{ReadWriter, macroRW}
  implicit final val readWriter: ReadWriter[LaunchGame] = macroRW
}
