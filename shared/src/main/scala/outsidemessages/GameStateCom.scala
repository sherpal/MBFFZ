package outsidemessages

import entities.Body
import gamestate.GameState

import upickle.default._

final case class GameStateCom(
                               startTime: Long,
                               time: Long,
                               zombiesPositions: List[(Double, Double)],
                               playersPositions: List[(Double, Double)],
                               obstaclesPositions: List[(Double, Double)],
                               popStationsPositions: List[(Double, Double)]
                             )


object GameStateCom {
  implicit val readWriter: ReadWriter[GameStateCom] = macroRW

  import scala.language.implicitConversions

  private implicit def bodyIterableToPosList(bodies: Iterable[Body]): List[(Double, Double)] =
    bodies.map(_.pos).map(_.toTuple).toList

  implicit class FromGameState(gameState: GameState) {
    def toGSCom: GameStateCom = GameStateCom(
      gameState.startTime, gameState.time,
      gameState.zombies.values,
      gameState.players.values,
      gameState.obstacles.map(obstacle => obstacle.shape.vertices.sum / obstacle.shape.vertices.length).map(_.toTuple),
      gameState.popStationContainer.popStations.values
    )

    def toGSComString: String = write(toGSCom)
  }
}