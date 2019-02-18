package gamedrawer

import org.scalajs.dom.raw.CanvasRenderingContext2D
import physics.Complex
import physics.shape.{Circle, Polygon}
import entities.{Obstacle, Player, Zombie, ZombiePopStation}
import gamemanager.Game
import gamestate.GameState
import org.scalajs.dom
import org.scalajs.dom.html
import scalatags.Text.all._
import scalatags.Text.all.{canvas => domCanvas}

import scala.collection.mutable

object GameDrawer {

  private lazy val canvas: html.Canvas = dom.document.getElementById("game-canvas").asInstanceOf[html.Canvas]
  private lazy val boundingRect = canvas.getBoundingClientRect()

  def showCanvas(): Unit = {
    dom.document.getElementById("pre-game-content").asInstanceOf[html.Div].style.display = "none"
    dom.document.getElementById("game-content").innerHTML = domCanvas(
      width := 800,
      height := 600,
      id := "game-canvas",
      tabindex := "1"
    ).render

    canvas.width = 800
    canvas.height = 600

    canvas.onmousemove = (mouseEvent: dom.MouseEvent) => {
      mousePosition = (
        mouseEvent.clientX - boundingRect.left,
        mouseEvent.clientY - boundingRect.top
      )
    }

    canvas.onkeydown = (keyboardEvent: dom.KeyboardEvent) => {
      pressedKeys += keyboardEvent.key
    }

    canvas.onkeyup = (keyboardEvent: dom.KeyboardEvent) => {
      pressedKeys -= keyboardEvent.key
    }

    canvas.focus()
  }

  private lazy val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  var mousePosition: (Double, Double) = (0, 0)

  def mouseComplexPosition: Complex = changeCoordinate(mousePosition._1, mousePosition._2)

  def clear(): Unit = {
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }

  def changeCoordinate(worldPos: Complex): (Double, Double) = (
    worldPos.re + canvas.width / 2, canvas.height / 2 - worldPos.im
  )

  def changeCoordinate(x: Double, y: Double): Complex = Complex(x - canvas.width / 2, canvas.height / 2 - y)

  private val pressedKeys: mutable.Set[String] = mutable.Set()
  def isPressed(direction: entities.Player.Direction): Boolean = direction.keys.exists(pressedKeys.contains)

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
    clear()
    drawObstacles(gameState.obstacles)
    drawPopStations(gameState.popStationContainer.popStations.values)
    drawZombies(gameState.zombies.values, time)
    drawPlayers(gameState.players.values)
  }

}
