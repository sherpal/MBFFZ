package outsidemessages

import entities.Player

// Possible directions are "Up", "Down", "Left", "Bottom"
final case class Direction(playerName: String, directions: List[String]) {
  def directionAndMoving: (Double, Boolean) = Player.findDirection(directions.map(Player.directionFromString))
}

object Direction {
  import upickle.default.{ReadWriter, macroRW}
  implicit final val readWriter: ReadWriter[Direction] = macroRW
}
