package entities

import gamestate.GameState
import physics.Complex
import physics.quadtree.ShapeQT
import physics.shape.{Polygon, Shape}

import scala.util.Random

final class Obstacle(val shape: Polygon, val time: Long) extends Body {

  val id: Long = Entity.newId()

  val pos: Complex = 0
  val rotation: Double = 0

}

object Obstacle {

  private val defaultObstacleRadius: Double = 30
  private val defaultShape: Polygon = Shape.regularPolygon(4, defaultObstacleRadius)

  def obstacle(position: Complex, time: Long): Obstacle =
    new Obstacle(Polygon(defaultShape.vertices.map(_ + position)), time)

  def createNewObstacle(time: Long, quadTree: ShapeQT): (Obstacle, Complex) = {
    def tryPosition(): (Obstacle, Complex) = {
      val w = GameState.worldBox.width - defaultObstacleRadius
      val h = GameState.worldBox.height - defaultObstacleRadius
      val pos = Complex(
        Random.nextDouble() * w - w / 2,
        Random.nextDouble() * h - h / 2
      )

      if (!quadTree.collides(defaultShape, pos, 0))
        (new Obstacle(Polygon(defaultShape.vertices.map(_ + pos), convex = true), time), pos)
      else
        tryPosition()
    }

    tryPosition()
  }

  def createSomeObstacles(obstacleNbr: Int, time: Long, quadTree: ShapeQT): (List[Obstacle], ShapeQT, List[Complex]) = {
    val (obstacles, qt, positions) = (1 to obstacleNbr).foldLeft((List[Obstacle](), quadTree, List[Complex]())) {
      case ((obs, qtAcc, pos), _) =>
        val (newObstacle, newPos) = createNewObstacle(time, qtAcc)
        (newObstacle +: obs, qtAcc :+ newObstacle.shape, newPos +: pos)
    }

    (obstacles, qt, positions)
  }

}