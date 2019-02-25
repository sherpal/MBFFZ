package outsidemessages

final case class YouHaveDied(time: Long)

object YouHaveDied {
  import upickle.default.{ReadWriter, macroRW}
  implicit final val readWriter: ReadWriter[YouHaveDied] = macroRW
}
