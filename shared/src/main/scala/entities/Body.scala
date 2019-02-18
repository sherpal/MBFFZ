package entities

import physics.Complex
import physics.shape.Shape

trait Body extends Entity {

  val shape: Shape
  val pos: Complex
  val rotation: Double

  def collides(that: Body): Boolean = shape.collides(pos, rotation, that.shape, that.pos, that.rotation)

  def collides(movingBody: MovingBody, time: Long): Boolean = shape.collides(
    pos, rotation, movingBody.shape, movingBody.currentPosition(time - movingBody.time), movingBody.rotation
  )

}
