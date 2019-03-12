package gamedrawer

import com.raquo.airstream.core.Observer
import com.raquo.airstream.ownership.Owner
import physics.Complex
import physics.shape.{Circle, Polygon}
import entities.{Obstacle, Player, Zombie, ZombiePopStation}
import gamedom.GameContent
import gamemanager.Game
import gamestate.GameState

import scala.collection.mutable

/**
  * This object is in charge of drawing the game in the canvas.
  *
  * The GameState is designed to be "self-contained", so it's the only thing that the game drawer needs
  * to draw the game.
  */
object GameDrawer extends Owner {

  private val canvas = GameContent()
  private val ctx = canvas.ctx

  def mouseComplexPosition: Complex = changeCoordinate(canvas.mousePosition.now())

  def changeCoordinate(worldPos: Complex): (Double, Double) = (
    worldPos.re + canvas.width / 2, canvas.height / 2 - worldPos.im
  )

  def changeCoordinate(x: Double, y: Double): Complex = Complex(x - canvas.width / 2, canvas.height / 2 - y)
  def changeCoordinate(pos: (Double, Double)): Complex = changeCoordinate(pos._1, pos._2)

  private val pressedKeys: mutable.Set[String] = mutable.Set()
  def isPressed(direction: entities.Player.Direction): Boolean = direction.keys.exists(pressedKeys.contains)
  canvas.$pressedKeysInfo.addObserver(Observer[(String, Boolean)] {
    case (key, true) => pressedKeys += key
    case (key, false) => pressedKeys -= key
  })(this)

  private val zombieColor: String = "#ccc"
  private val obstacleColor: String = "white"
  private val popStationColor: String = "red"

  private def polygonPath(
                           polygon: Polygon,
                           translation: Complex, rotation: Double
                         ): Unit = {
    val canvasCoordinates = polygon.vertices.map(translation + _ * Complex.rotation(rotation))
      .map(changeCoordinate)

    ctx.beginPath()
    ctx.moveTo(canvasCoordinates(0)._1, canvasCoordinates(0)._2)
    for ((x, y) <- canvasCoordinates.tail) {
      ctx.lineTo(x, y)
    }
    ctx.closePath()
    ctx.fill()
  }

  private def circlePath(
                          circle: Circle,
                          translation: Complex
                        ): Unit = {
    ctx.beginPath()
    val (x, y) = changeCoordinate(translation)

    ctx.arc(x, y, circle.radius, 0, 2 * math.Pi)
    ctx.closePath()
    ctx.fill()
  }

  private def drawZombies(zombies: Iterable[Zombie], time: Long): Unit = {
    ctx.fillStyle = zombieColor
    for (zombie <- zombies) {
      polygonPath(zombie.shape, zombie.currentPosition(time - zombie.time), zombie.rotation)
    }
  }

  private def drawPlayers(players: Iterable[Player]): Unit = {
    for (player <- players) {
      ctx.fillStyle = Player.cssColour(player.colour)
      circlePath(player.shape, player.pos)
    }
  }

  private def drawObstacles(obstacles: List[Obstacle]): Unit = {
    ctx.fillStyle = obstacleColor
    for (obstacle <- obstacles) {
      polygonPath(obstacle.shape, 0, 0)
    }
  }

  private def drawPopStations(popStations: Iterable[ZombiePopStation]): Unit = {
    ctx.fillStyle = popStationColor
    for (station <- popStations) {
      circlePath(station.shape, station.pos)
    }
  }

  def drawGameState(gameState: GameState): Unit = {
    val time = Game.game.getTime
    canvas.clear()
    drawObstacles(gameState.obstacles)
    drawPopStations(gameState.popStationContainer.popStations.values)
    drawZombies(gameState.zombies.values, time)
    drawPlayers(gameState.players.values)
  }

}
